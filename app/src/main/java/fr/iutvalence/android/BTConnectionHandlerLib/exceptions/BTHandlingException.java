package fr.iutvalence.android.BTConnectionHandlerLib.exceptions;

/**
 * Created by raievskc on 2/14/16.
 */
public class BTHandlingException extends Exception{

   public BTHandlingException(String detailMessage) {
      super(detailMessage);
   }

   public enum Reason{SEND_DATA_FAILED_IO, SEND_DATA_FAILED_INIT, CONNECTION_STATUS_BT_NOT_ACTIVATED, CONNECTION_STATUS_NO_BT_DEVICE_FOUND, CONNECTION_STATUS_NO_BT_ADAPTER}
   public enum OpenConnectionStatus {CONNECTION_STATUS_OPENED, CONNECTION_STATUS_BT_NOT_ACTIVATED, CONNECTION_STATUS_CLOSED, CONNECTION_STATUS_NO_BT_DEVICE_FOUND, CONNECTION_STATUS_NO_BT_ADAPTER}

}
