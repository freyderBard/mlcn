package com.ericsson.mlcn.im.rules;

import com.ericsson.mlne.im.common.config.LoadConfigurationBasicCallback;
import com.ericsson.mlne.im.common.config.LoadConfigurationBasicCallback.RestoreBackupValidationException;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.im.common.IPAddress;
import no.axxessit.mgt.MoAttributeList;

public class MLCNLoadConfigurationCallback extends LoadConfigurationBasicCallback {

   private TranslationManager tm = TranslationManager.getInstance();


   protected String getOperationCommandAttrName() {
      return "ConfigLoadCommand";
   }

   protected String getUploadFileNameAttrName() {
      return "BackupConfigFileName";
   }

   protected String getDownloadFileNameAttrName() {
      return "ConfigFileName";
   }

   public void postSave(ApplicationContext appContext, MoAttributeList[] list) {
      this.acceptConfiguration(true);
   }

   protected String[] getOperAttributes() {
      return new String[]{"ConfigLoadCommand", "BackupConfigFileName", "ConfigFileName", "ButtonBrowser", "ftpsettings", "RestoreButtonBrowser"};
   }

   protected void validateAdditionalConfig(ApplicationContext appContext, MoAttributeList[] list) throws RestoreBackupValidationException {
      for(int i = 0; i < list.length; ++i) {
         if(list[i].getMOID().getClassname().equals("com.ericsson.mlne.im.FtpConfig")) {
            this.checkFTPServer(appContext, list[i]);
            break;
         }
      }

   }

   protected void setOperationStatusBeforeSave() {
      if(this.configLoadCmd.value() == BACKUP_INT) {
         this.formModel.getNode(this.getDownloadFileNameAttrName()).setEdited(false);
         this.formModel.getNode(this.getOperationCommandAttrName()).setEdited(true);
         this.formModel.getNode(this.getUploadFileNameAttrName()).setEdited(true);
      } else if(this.configLoadCmd.value() == RESTORE_INT) {
         this.formModel.getNode(this.getUploadFileNameAttrName()).setEdited(false);
         this.formModel.getNode(this.getOperationCommandAttrName()).setEdited(true);
         this.formModel.getNode(this.getDownloadFileNameAttrName()).setEdited(false);
      }

   }

   private void checkFTPServer(ApplicationContext appContext, MoAttributeList list) throws RestoreBackupValidationException {
      String ftpServer = (String)list.getAttribute("FTPAddress2").getValue();
      if(ftpServer.length() == 0) {
         appContext.showErrorDialog(this.tm.getString("$MLCN.RESTORE_BACKUP"), this.tm.getString("$MLCN.FTP_SERVER_EMPTY"));
         throw new RestoreBackupValidationException(this);
      } else {
         boolean ret = IPAddress.validate(ftpServer);
         if(!ret) {
            appContext.showErrorDialog(this.tm.getString("$MLCN.RESTORE_BACKUP"), this.tm.getString("$MLCN.FTP_IP_ERROR"));
            throw new RestoreBackupValidationException(this);
         } else {
            String ftpUserName2 = list.getAttribute("FTPUserName2").getValue().toString();
            String ftpPassword2 = list.getAttribute("FTPPassword2").getValue().toString();
            if(ftpUserName2 != null && !ftpUserName2.equals("")) {
               if(ftpUserName2 != null && !"anonymous".equals(ftpUserName2) && (ftpPassword2 == null || "".equals(ftpPassword2))) {
                  appContext.showErrorDialog(this.tm.getString("$MLCN.RESTORE_BACKUP"), this.tm.getString("$MLCN.FTP_PWD_EMPTY"));
                  throw new RestoreBackupValidationException(this);
               }
            } else {
               appContext.showErrorDialog(this.tm.getString("$MLCN.RESTORE_BACKUP"), this.tm.getString("$MLCN.FTP_USERNAME_EMPTY"));
               throw new RestoreBackupValidationException(this);
            }
         }
      }
   }
}
