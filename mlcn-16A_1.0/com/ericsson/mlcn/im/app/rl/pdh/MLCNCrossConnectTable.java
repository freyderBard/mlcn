package com.ericsson.mlcn.im.app.rl.pdh;

import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.common.util.StaticSnmpIdxHelper;
import com.ericsson.mlcraft.common.util.Util;
import com.ericsson.mlne.im.fa.traffic_routing.CrossConnectTable;
import com.ericsson.mlne.utils.MMUUtil;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;

public class MLCNCrossConnectTable extends CrossConnectTable {

   private static final long serialVersionUID = -8136352679076959793L;


   public MLCNCrossConnectTable(MBeanInfo info) {
      super(info);
   }

   public String getMLCNInterface1() {
      int ifIndex = ((Integer)this.getAttribute("Interface1Index")).intValue();
      MOID interfaceMoid = new MOID("com.ericsson.mlne.im.fa.traffic_routing.X1_Overview", "X1:" + ifIndex, this.moid.getResourceId());
      return (String)this.getAttribute(interfaceMoid, "MocMoi");
   }

   public String getMLCNInterface2() {
      int ifIndex = ((Integer)this.getAttribute("Interface1Index")).intValue();
      MOID interfaceMoid = new MOID("com.ericsson.mlne.im.fa.traffic_routing.X1_Overview", "X1:" + ifIndex, this.moid.getResourceId());
      long indexInterface1 = (long)Util.getIndex(interfaceMoid);
      long slotNo = StaticSnmpIdxHelper.instance().getSlotNo(indexInterface1);
      long ifIdxStart = StaticSnmpIdxHelper.instance().createIfStaticIndex(127L, 7L, 15L, 0L, slotNo, 0L);
      AxxEnum protType = null;

      try {
         MOID interface1 = RelationSearchUtil.getTerminal(MMUUtil.getMMUForCN(MgtUtil.getInstance().getNe(interfaceMoid), true, true));
         protType = (AxxEnum)MgtUtil.getInstance().getAttribute(interface1, "Protection");
      } catch (Exception var13) {
         protType = null;
      }

      String interface11 = (String)this.getAttribute(interfaceMoid, "MocMoi");
      if(protType.value() == 3 || protType.value() == 4) {
         String[] interfaceParts = interface11.split("\\/");
         int protectionSlot = Integer.parseInt(interfaceParts[1]) + 1;
         interface11 = interfaceParts[0] + "/" + interfaceParts[1] + "+" + protectionSlot + "/" + interfaceParts[2];
      }

      return interface11.trim().substring(0, interface11.length() - 2) + (indexInterface1 - ifIdxStart + 1L);
   }
}
