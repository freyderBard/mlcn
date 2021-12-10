package com.ericsson.mlcn.im;

import com.ericsson.mlcn.im.NetSyncTable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import no.axxessit.common.Env;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.il.snmp.OperationMappingEntry;
import no.axxessit.il.snmp.SnmpAdapter;
import no.axxessit.il.snmp.SnmpTable;
import no.axxessit.il.snmp.strategy.RowCreateAndGoStrategy;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;

public class NetSyncCreateAndGoStrategy extends RowCreateAndGoStrategy {

   private static final Integer PDH_TRAFFIC = Integer.valueOf(1);
   private static final Integer SYNC_INPUT_2MHz = Integer.valueOf(2);
   private static final Integer SYNC_INPUT_2Mbps = Integer.valueOf(3);
   private static final Integer SYNC_OUTPUT_2MHz = Integer.valueOf(4);
   private static final Integer SYNC_OUTPUT_2Mbps = Integer.valueOf(5);
   private static final String NET_SYNC_TABLE_CLASS_NAME = NetSyncTable.class.getName();
   private static final String NET_SYNC_MO = "xfCNDsx1Mode";


   public Object invoke(Object[] params, SnmpAdapter adapter, OperationMappingEntry entry) {
      AttributeList attributes = (AttributeList)params[1];
      Attribute syncModeAttr = attributes.getAttribute("SyncMode");
      AxxEnum syncMode = (AxxEnum)syncModeAttr.getValue();
      MOID neMoid = (MOID)Env.getInstance().getObject("EQUIPMENT.MOID");
      MOID returnMoid = null;
      if(!PDH_TRAFFIC.equals(syncMode.getValue())) {
         Map rowsInxfCNDsx1ConfigXTable = this.queryRowsInXFCNDsx1ConfigXTable(adapter);
         Map e1Indices = this.mapRowIndicesToE1(rowsInxfCNDsx1ConfigXTable);
         String indexOf16thE1;
         if(this.isInput(syncMode)) {
            indexOf16thE1 = (String)e1Indices.get(NetSyncCreateAndGoStrategy.E1.E1_16TH);
            String syncModeOf16thE1 = "xfCNDsx1Mode." + indexOf16thE1;
            adapter.setAttribute(syncModeOf16thE1, syncMode.getValue(), (Object)null);
            returnMoid = new MOID(NET_SYNC_TABLE_CLASS_NAME, "xfCNDsx1ConfigXEntry:" + indexOf16thE1, neMoid.getResourceId());
         } else {
            indexOf16thE1 = (String)e1Indices.get(NetSyncCreateAndGoStrategy.E1.E1_16TH);
            Integer syncModeOf16thE11 = (Integer)rowsInxfCNDsx1ConfigXTable.get(indexOf16thE1);
            String oid;
            if(!SYNC_OUTPUT_2MHz.equals(syncModeOf16thE11) && !SYNC_OUTPUT_2Mbps.equals(syncModeOf16thE11)) {
               oid = "xfCNDsx1Mode." + indexOf16thE1;
               adapter.setAttribute(oid, syncMode.getValue(), (Object)null);
               returnMoid = new MOID(NET_SYNC_TABLE_CLASS_NAME, "xfCNDsx1ConfigXEntry:" + indexOf16thE1, neMoid.getResourceId());
            } else {
               oid = (String)e1Indices.get(NetSyncCreateAndGoStrategy.E1.E1_15TH);
               Integer syncModeOf15thE1 = (Integer)rowsInxfCNDsx1ConfigXTable.get(oid);
               String oid1;
               if(!SYNC_OUTPUT_2MHz.equals(syncModeOf15thE1) && !SYNC_OUTPUT_2Mbps.equals(syncModeOf15thE1)) {
                  oid1 = "xfCNDsx1Mode." + oid;
                  adapter.setAttribute(oid1, syncMode.getValue(), (Object)null);
                  returnMoid = new MOID(NET_SYNC_TABLE_CLASS_NAME, "xfCNDsx1ConfigXEntry:" + oid, neMoid.getResourceId());
               } else {
                  oid1 = (String)e1Indices.get(NetSyncCreateAndGoStrategy.E1.E1_14TH);
                  String oid2 = "xfCNDsx1Mode." + oid1;
                  adapter.setAttribute(oid2, syncMode.getValue(), (Object)null);
                  returnMoid = new MOID(NET_SYNC_TABLE_CLASS_NAME, "xfCNDsx1ConfigXEntry:" + oid1, neMoid.getResourceId());
               }
            }
         }
      }

      return returnMoid;
   }

   private Map queryRowsInXFCNDsx1ConfigXTable(SnmpAdapter snmpAdapter) {
      SnmpTable table = snmpAdapter.getTable("xfCNDsx1ConfigXEntry", new String[]{"xfCNDsx1Mode"}, 3, (Object)null);
      int rowNumber = table.getRowCount();
      SnmpTable indexTable = table.getIndexTable();
      HashMap rows = new HashMap();
      if(rowNumber == 3) {
         for(int i = 0; i < rowNumber; ++i) {
            String index = (String)indexTable.getValueAt(i, 0);
            Integer syncMode = (Integer)table.getValueAt(i, 0);
            rows.put(index, syncMode);
         }
      }

      return rows;
   }

   private Map mapRowIndicesToE1(Map rows) {
      HashMap e1 = new HashMap();
      if(rows != null && rows.size() == 3) {
         Set indices = rows.keySet();
         String[] indicesArray = new String[3];
         indices.toArray(indicesArray);
         Arrays.sort(indicesArray);
         e1.put(NetSyncCreateAndGoStrategy.E1.E1_14TH, indicesArray[0]);
         e1.put(NetSyncCreateAndGoStrategy.E1.E1_15TH, indicesArray[1]);
         e1.put(NetSyncCreateAndGoStrategy.E1.E1_16TH, indicesArray[2]);
      }

      return e1;
   }

   private boolean isInput(AxxEnum syncMode) {
      boolean isInput = syncMode != null && (SYNC_INPUT_2MHz.equals(syncMode.getValue()) || SYNC_INPUT_2Mbps.equals(syncMode.getValue()));
      return isInput;
   }


   private static enum E1 {

      E1_14TH("E1_14TH", 0),
      E1_15TH("E1_15TH", 1),
      E1_16TH("E1_16TH", 2);
      // $FF: synthetic field
      private static final NetSyncCreateAndGoStrategy.E1[] $VALUES = new NetSyncCreateAndGoStrategy.E1[]{E1_14TH, E1_15TH, E1_16TH};


      private E1(String var1, int var2) {}

   }
}
