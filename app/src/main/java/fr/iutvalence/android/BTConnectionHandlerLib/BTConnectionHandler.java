package fr.iutvalence.android.BTConnectionHandlerLib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import fr.iutvalence.android.BTConnectionHandlerLib.exceptions.BTAdapterDisabledHException;
import fr.iutvalence.android.BTConnectionHandlerLib.exceptions.BTHandlingException;
import fr.iutvalence.android.BTConnectionHandlerLib.exceptions.NoAdapterBTHException;
import fr.iutvalence.android.BTConnectionHandlerLib.exceptions.NoBTDeviceFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * This class handles a Bluetooth connection.
 *
 * It allows the user to select a BT device, to open a socket connection with
 * this selected device and to send data through this connection.
 *
 * It also allows the user to create a socket server, start accepting connections on it
 * and read data from incoming connection.
 */
public class BTConnectionHandler {

   // standard Bluetooth serial port service ID
   private static final UUID sBTSerialCommUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

   private BluetoothAdapter mBluetoothAdapter = null;
   private BluetoothSocket mSocket = null;
   private BluetoothServerSocket mServerSocket;
   private BluetoothDevice mDevice = null;
   private OutputStream mmOutputStream = null;
   private InputStream mmInputStream = null;
   private Thread mWorkerThread = null;
   volatile boolean stopWorker = false;

   public BTConnectionHandler(Context context) {
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
   }

   /**
    * Try to open a connection with another Bluetooth device with the given name.
    *
    * @param pDeviceName Name of the Bluetooth device to connect with. This device must have been paired with
    *                    the one requesting a connection before calling this method.
    * @throws BTHandlingException Throws the exceptions thrown by the called functions.
    * @throws IOException Throws the exceptions thrown by the called functions.
    * @see {@link #checkBTAdapter()}
    * @see {@link #selectBTDevice(String)}
    * @see {@link #openBTSocketWithSelectedDevice()}
    */
   public void connectToBTDevice(String pDeviceName) throws BTHandlingException, IOException {
         checkBTAdapter();
         selectBTDevice(pDeviceName);
         openBTSocketWithSelectedDevice();
   }


   /**
    * Check whether there is a Bluetooth adapter on the current device and if this adapter is enabled.
    *
    * @throws BTHandlingException
    */
   public void checkBTAdapter() throws BTHandlingException {
      if (mBluetoothAdapter == null) {
         throw new NoAdapterBTHException();
      }

      if (!mBluetoothAdapter.isEnabled()) {
         throw new BTAdapterDisabledHException();
      }

   }

   /***
    *  Find and Store BlueTooth device named Makeblock for later use*/

   /**
    * Check if there is a Bluetooth device paired with the current one which name matches the given one.
    * Keep a reference of this device if found.
    *
    * @param pDeviceName Name of the device to select.
    * @throws NoBTDeviceFoundException
    */
   void selectBTDevice(String pDeviceName) throws NoBTDeviceFoundException {
      if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
         Log.e("BTConnectionHandler", "selectBTDevice : bluetooth adapter not usable, checkBTAdapter must be called before selectBTDevice.");
         return;
      }

      Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
      Log.d("BTConnectionHandler","Number of paired devices :" + pairedDevices.size());
      if (pairedDevices.size() > 0) {
         Log.d("BTConnectionHandler", "Listing paired devices");

         for (BluetoothDevice device : pairedDevices) {

            Log.d("BTConnectionHandler", "Device [" + device.getName() + "]");
            if (pDeviceName == null || pDeviceName.length() == 0)
            {
               // Take the first device found if no valid device name is given
               mDevice = device;
               break;
            }
            else if (device.getName().equals(pDeviceName)) {
               mDevice = device;
               break;
            }
         }

         if (mDevice != null)
         {
            Log.d("BTConnectionHandler","Using Bluetooth Device [" + mDevice.getName() + "]");
         }
         else
         {
            Log.d("BTConnectionHandler","No Bluetooth Device named [" + pDeviceName + "] found.");
            throw new NoBTDeviceFoundException();
         }
      }
      else
      {
         throw new NoBTDeviceFoundException();
      }
   }

   /**
    * Open a Bluetooth connection with the device selected by the {@link #selectBTDevice(String)} function.
    * @throws IOException
    * @throws NoBTDeviceFoundException
    */
   void openBTSocketWithSelectedDevice() throws IOException, NoBTDeviceFoundException {
      if (mDevice != null) {
         // Open a connection with selected device using the standard BT serial port service ID
         mSocket = mDevice.createRfcommSocketToServiceRecord(sBTSerialCommUUID);
         Log.d("MakeBlockBlueTooth", "Socket created with device [" + mDevice.getName() + "]");

         mSocket.connect();
         Log.d("MakeBlockBlueTooth", "Socket connected with device [" + mDevice.getName() + "]");
         mmOutputStream = mSocket.getOutputStream();
         mmInputStream = mSocket.getInputStream();
      }
      else
      {
         throw new NoBTDeviceFoundException();
      }
   }

   /**
    * Close the currently opened connection. Either as a server or a client.
    * @return false if a I/O Exception occured while closing sockets, true otherwise.
    */
   public boolean closeConnection() {
      try {
         stopWorker = true;
         if (mWorkerThread != null && mServerSocket != null)
         {
            // This should cause the worker thread responsible for listening for data
            // on the socket to stop waiting on the read and die.
            mServerSocket.close();
         }
         if (mmOutputStream != null) {
            mmOutputStream.close();
            mmOutputStream = null;
         }
         if (mmInputStream != null) {
            mmInputStream.close();
            mmInputStream = null;
         }
         if (mSocket != null) {
            mSocket.close();
         }
      } catch (IOException ex) {
         Log.d("BTConnectionHandler", "Closing bluetooth connexion failed with IO Exception");
         return false;
      }
      return true;
   }

   /**
    * Send data on the socket opened by {@link #openBTSocketWithSelectedDevice()}.
    *
    * @param msg Data to send.
    * @throws IOException
    * @throws BTHandlingException
    */
   public void sendData(String msg) throws IOException, BTHandlingException {
      if (mmOutputStream != null && mBluetoothAdapter != null && mDevice != null) {
         mmOutputStream.write(msg.getBytes());
      }
      else {
         throw new BTHandlingException("Data sending failed");
      }
   }

   /**
    * Interface for the callback object that will be used to transfer data read
    * on the server socket created by {@link #createBTSocketServer(OnBytesReadListener)}
    * to the calling object.
    * Useful only for receiving data on a server socket.
    */
   public interface OnBytesReadListener{
      void onBytesRead(String bytesRead);
   }

   /**
    * Create a Bluetooth socket server bound to the default service UUID associated with a serial link.
    * Useful for receiving data.
    * @param onBytesReadListener callback object on which the onBytesRead method will be called upon data reception.
    * @throws BTHandlingException
    * @throws IOException
    */
   public void createBTSocketServer(OnBytesReadListener onBytesReadListener) throws BTHandlingException, IOException {
      checkBTAdapter();

      mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Echo for Debug", sBTSerialCommUUID);

      listenForBTData(onBytesReadListener);
   }

   /**
    * Start listening on the Bluetooth socket server created by {@link #createBTSocketServer(OnBytesReadListener)}.
    * Received data will be passed to the onBytesRead function of the given OnBytesReadListener.
    *
    * @param onBytesReadListener callback object on which the onBytesRead method will be called upon data reception.
    * @throws BTHandlingException
    * @throws IOException
    */
   void listenForBTData(final OnBytesReadListener onBytesReadListener) throws BTHandlingException, IOException {
      checkBTAdapter();

      if (mServerSocket == null)
      {
         throw new BTHandlingException("Server Scoket not created. createBTSocketServer must be called before listenForBTData.");
      }

      final Handler handler = new Handler();
      stopWorker = false;

      mWorkerThread = new Thread(new Runnable() {
         public void run() {

            try {
               BluetoothSocket lServerSocket = mServerSocket.accept();
               InputStream lInputStream = lServerSocket.getInputStream();

               while(!stopWorker) {

                  byte[] packetBytes = new byte[100];
                  int nbRead = lInputStream.read(packetBytes);
                  byte[] encodedBytes = new byte[nbRead];
                  System.arraycopy(packetBytes, 0, encodedBytes, 0, nbRead);
                  final String data = new String(encodedBytes, "US-ASCII");
                  Log.d("BTConnectionHandler", "Read data: [" + data + "]");
                  handler.post(new Runnable() {
                     public void run() {
                        onBytesReadListener.onBytesRead(data);
                     }
                  });
               }
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      });

      mWorkerThread.start();
   }
}

