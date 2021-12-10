package com.ericsson.mlcn.im.rules;

import com.ericsson.client.appl.MLCraftFormViewer;
import com.ericsson.mlcraft.common.util.EnhancedAbstractDefaultRule;
import com.ericsson.mlcraft.common.util.StringUtils;
import java.util.Collection;
import java.util.Iterator;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.forms.AxxFormModel;
import no.axxessit.client.gui.forms.AxxFormNode;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.as.ServiceLocator;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxEnumConstraint;
import no.axxessit.common.val.AxxString;
import no.axxessit.il.snmp.SnmpResource;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MBeanContainer;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.ManagedResource;
import no.axxessit.mgt.rule.event.AttributeValueChangeEvent;
import no.axxessit.mgt.rule.inf.IContextProvider;

public class MLCNLoadConfigurationRule extends EnhancedAbstractDefaultRule {

   private TranslationManager tm = TranslationManager.getInstance();
   private static String FROM_PATH = "../res/forms/MLCN/LoadConfiguration.form";
   private AxxEnum oldFTP;
   private AxxString oldFileName;
   private AxxEnum oldOperation;
   private AxxFormModel formModel;


   public AttributeList evaluate(Object sourceContext, AttributeList list, IContextProvider callback, Object userData) {
      MOID moid = null;
      ManagedResource mr = this.getSnmpResource();
      MLCraftFormViewer view = (MLCraftFormViewer)ApplicationManager.getInstance().getApplication(MLCraftFormViewer.class);
      this.formModel = view.getModel();
      if(!this.formModel.getAddress().getPath().endsWith(FROM_PATH)) {
         return null;
      } else if(sourceContext instanceof MOID) {
         moid = (MOID)sourceContext;
         if(list.getAttribute("ConfigFileName") != null && this.formModel.getNode("FTPActive").getValue() != null) {
            if(this.isViewUser()) {
               this.disableAttributes(moid, new String[]{"buttonPC"});
            }

            AxxEnum currentOperation = (AxxEnum)list.getAttribute("ConfigLoadCommand").getValue();
            AxxEnum ftpActive = (AxxEnum)this.formModel.getNode("FTPActive").getValue();
            AxxString currentFileName = (AxxString)list.getAttribute("ConfigFileName").getValue();
            if(this.oldOperation == null) {
               this.oldOperation = currentOperation;
            }

            if(this.oldFTP == null) {
               this.oldFTP = ftpActive;
            }

            if(this.oldFileName == null) {
               this.oldFileName = currentFileName;
            }

            Object tmp = list.getAttribute("BackupConfigFileName").getValue();
            String backupConfigFileNameValue = tmp instanceof AxxString?((AxxString)tmp).value():tmp.toString();
            int loadCommandValue = currentOperation.value();
            AxxEnumConstraint loadCommandConstOld = (AxxEnumConstraint)currentOperation.getConstraint();
            AxxEnum configStatus;
            if(loadCommandConstOld != null && loadCommandConstOld.containsValue(0) && loadCommandValue != 0) {
               AxxEnumConstraint e = (AxxEnumConstraint)loadCommandConstOld.clone();
               e.removeValue(0);
               configStatus = new AxxEnum(loadCommandValue, e);
               AttributeValueChangeEvent event = new AttributeValueChangeEvent(sourceContext, "ConfigLoadCommand", (Object)null, configStatus);
               this.firePropertyChange(event);
            }

            try {
               Object e1 = MgtUtil.getInstance().getAttribute(moid, "ConfigStatus");
               if(e1 != null) {
                  configStatus = new AxxEnum(((Integer)e1).intValue(), "CONFIG_STATUS");
                  if(configStatus.value() == 1) {
                     this.changeNodesStatus(this.formModel, new String[]{"ConfigLoadCommand", "ftpsettings", "BackupConfigFileName", "ConfigFileName"}, "enabled", Boolean.valueOf(false));
                     this.formModel.getNode("StatusText").setProperty("text", this.tm.getString("$MLCN.BACKUP_ONGOING"));
                     this.formModel.getNode("StatusGroup").setProperty("visible", Boolean.valueOf(true));
                  }

                  if(configStatus.value() == 4) {
                     this.changeNodesStatus(this.formModel, new String[]{"ConfigLoadCommand", "ftpsettings", "BackupConfigFileName", "ConfigFileName"}, "enabled", Boolean.valueOf(false));
                     this.formModel.getNode("StatusText").setProperty("text", this.tm.getString("$MLCN.RESTORE_ONGOING"));
                     this.formModel.getNode("StatusGroup").setProperty("visible", Boolean.valueOf(true));
                  }
               }
            } catch (Exception var18) {
               var18.printStackTrace();
            }

            if(!StringUtils.isEmpty(backupConfigFileNameValue) && currentOperation.compareTo(this.oldOperation) != 0 && currentOperation.value() == 1) {
               this.formModel.getNode("StatusGroup").setProperty("visible", Boolean.valueOf(false));
               mr.setAttribute(moid, "BackupConfigFileName", backupConfigFileNameValue);
            }

            this.oldOperation = currentOperation;
            this.oldFTP = ftpActive;
            this.oldFileName = currentFileName;
            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void changeNodesStatus(AxxFormModel formModel, String[] attributes, String property, Object value) {
      String[] arr$ = attributes;
      int len$ = attributes.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String attr = arr$[i$];
         AxxFormNode node = formModel.getNode(attr);
         if(node != null) {
            node.setProperty(property, value);
         }
      }

   }

   public String[] getSourceAttributeNames() {
      this.getPropertyChangeListeners();
      return new String[]{"ConfigLoadCommand"};
   }

   private ManagedResource getSnmpResource() {
      MBeanContainer c = (MBeanContainer)ServiceLocator.getInstance().lookup("MBeanContainer", false);
      Collection resources = c.getMResources();
      if(resources != null) {
         Iterator iterator = resources.iterator();

         while(iterator.hasNext()) {
            ManagedResource resource = (ManagedResource)iterator.next();
            if(resource instanceof SnmpResource) {
               return resource;
            }
         }
      }

      return null;
   }

   public String getDestinationAttributeName() {
      return "ConfigLoadCommand";
   }

   private boolean isViewUser() {
      String viewUserFeature = TranslationManager.getInstance().getString("$GUEST_USER_FEATURE");
      return MgtUtil.getInstance().isFeatureEnabled(viewUserFeature);
   }

}
