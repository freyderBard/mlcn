package com.ericsson.mlcn.im.rules;

import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.forms.AxxFormContainer;
import no.axxessit.client.gui.forms.IFormHandler;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.ManagementServer;
import no.axxessit.mgt.TableRef;

public class InputSyncSignalHandler implements IFormHandler {

   private static final String ATTR_SYNC_MODE = "SyncMode";
   private static final String ATTR_QUALITY_LEVEL = "SyncNomAssQualityLevel";
   private static final String ATTR_IS_NOT_ASSIGNED_DEFAULT_VALUE = "IsNotAssignedTheDefaultValue";
   private static final String ATTR_NET_SYNC_TABLE = "NetSyncTable";
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);


   public void installHandler(ApplicationContext appContext, AxxFormContainer form) {
      ManagementServer managementServer = MgtUtil.getInstance().getMgtService();
      MOID moid = form.getModel().getMOID();

      try {
         AxxEnum e = (AxxEnum)managementServer.getAttribute(moid, "SyncNomAssQualityLevel");
         boolean isNotAssignedTheDefaultValue = ((Boolean)managementServer.getAttribute(moid, "IsNotAssignedTheDefaultValue")).booleanValue();
         if(isNotAssignedTheDefaultValue && 5 == e.value()) {
            Object o = managementServer.getAttribute(moid, "NetSyncTable");
            if(o instanceof TableRef) {
               TableRef tr = (TableRef)o;
               AxxMBTableMBean netSyncTable = (AxxMBTableMBean)MBeanProxy.createInstance(tr.getMoid(), managementServer);
               MOID[] arr$ = netSyncTable.getRows();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  MOID row = arr$[i$];
                  AxxEnum syncMode = (AxxEnum)managementServer.getAttribute(row, "SyncMode");
                  if(null != syncMode && this.isInput(syncMode)) {
                     String msg = "Quality Level should be changed to something other than Not Assigned.";
                     String title = "Quality Level Warning";
                     appContext.showWarningDialog(msg, title, 3);
                     break;
                  }
               }
            }
         }
      } catch (Exception var17) {
         Logger.getLogger().error(var17);
      }

   }

   public void uninstallHandler(ApplicationContext appContext, AxxFormContainer form) {}

   private boolean isInput(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }

}
