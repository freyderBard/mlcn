package com.ericsson.mlcn.im.rules;

import com.ericsson.mlcraft.common.license.FormSaveCallback;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.im.common.IPAddress;
import no.axxessit.mgt.MoAttributeList;

public class ErrorLogCallback extends FormSaveCallback {

   private static final String ERROR_LOG_ERROR = "Error Log - Error";
   private static final String NO_IP_ADDRESS_FTP = "FTP Server must not be empty.";
   private static final String ILLEGAL_IP_ADDRESS_FTP = "FTP Server must contain a valid IP address.";
   private static final String NO_USER_NAME_FTP = "Empty User Name is not allowed.";
   private static final String NO_PASSWORD_FTP = "Empty Password is not allowed.";
   private static final String ILLEGAL_IP_ADDRESS_MASK = "255.255.255.255";
   private static final String ILLEGAL_IP_ADDRESS_ZERO = "0.0.0.0";


   public boolean validate(ApplicationContext appContext, MoAttributeList[] list) {
      boolean ret = true;
      MoAttributeList[] arr$ = list;
      int len$ = list.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MoAttributeList mlist = arr$[i$];
         if(MgtUtil.getInstance().getMgtService().isInstanceOf(mlist.getMOID(), "com.ericsson.mlne.im.FtpConfig")) {
            String ftpServer = (String)mlist.getAttribute("FTPAddress2").getValue();
            if(ftpServer.length() == 0) {
               appContext.showErrorDialog("Error Log - Error", "FTP Server must not be empty.");
               return false;
            }

            ret = IPAddress.validate(ftpServer);
            if(!ret || ftpServer.equals("255.255.255.255") || ftpServer.equals("0.0.0.0")) {
               appContext.showErrorDialog("Error Log - Error", "FTP Server must contain a valid IP address.");
               return false;
            }

            String ftpSevUName = (String)mlist.getAttribute("FTPUserName2").getValue();
            if(ftpSevUName.length() == 0) {
               appContext.showErrorDialog("Error Log - Error", "Empty User Name is not allowed.");
               return false;
            }

            String ftpSevPwd = mlist.getAttribute("FTPPassword2").getValue().toString();
            if(ftpSevPwd.length() == 0) {
               appContext.showErrorDialog("Error Log - Error", "Empty Password is not allowed.");
               return false;
            }
         }
      }

      return true;
   }
}
