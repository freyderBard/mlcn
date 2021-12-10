package com.ericsson.mlcn.im.physical;

import com.ericsson.mlcraft.common.util.Util;
import java.util.HashMap;
import java.util.Map;
import no.axxessit.client.appl.license.LicenseAttributeIndexHandler;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.TableRef;

public class UserIOIndexHandler implements LicenseAttributeIndexHandler {

   public Map retrieveSnmpMoIndex(MOID moid, String attribute) {
      HashMap indexes = new HashMap();
      MOID[] arr$ = this.getMOIDs(this.getTableRef(moid, attribute));
      int len$ = arr$.length;
      byte i$ = 0;
      if(i$ < len$) {
         MOID tableMoid = arr$[i$];
         indexes.put(attribute, Util.getIndexString(tableMoid));
      }

      return indexes;
   }

   private MOID[] getMOIDs(TableRef ref) {
      if(ref == null) {
         return new MOID[0];
      } else {
         AxxMBTableMBean mbean = null;

         try {
            mbean = (AxxMBTableMBean)MBeanProxy.createInstance(ref.getMoid(), MgtUtil.getInstance().getMgtService());
         } catch (ClassNotFoundException var4) {
            Logger.getLogger().error(var4);
         }

         return mbean instanceof AxxMBTableMBean?mbean.getRows():new MOID[0];
      }
   }

   private TableRef getTableRef(MOID moid, String tableName) {
      Object result = null;

      try {
         result = MgtUtil.getInstance().getAttribute(moid, tableName);
      } catch (Exception var5) {
         Logger.getLogger().error(var5);
      }

      return result instanceof TableRef?(TableRef)result:null;
   }
}
