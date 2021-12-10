package com.ericsson.mlcn.im.app.rl.pdh;

import com.ericsson.mlcraft.common.tree.ITreeMenuLang;
import com.ericsson.mlcraft.common.util.MLCraftConstants;
import com.ericsson.mlcraft.common.util.MLNEUtil;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlne.im.MLNE.NodeType;
import com.ericsson.mlne.im.common.MrBean;
import com.ericsson.mlne.utils.MMUUtil;
import java.util.HashMap;
import java.util.Map;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.TableRef;

public class PDH extends MrBean implements ITreeMenuLang, MLCraftConstants {

   private static final long serialVersionUID = 5677225892692572176L;


   public PDH(MBeanInfo info) {
      super(info);
   }

   public Map retrieveLicenseAttributes() {
      return new HashMap();
   }

   public AttRef getX1Config() {
      MOID X1ConfigAttributeClass = new MOID("com.ericsson.mlne.im.common.interfaces.X1Config", "*:*", this.resource.getRID());
      MOIDList m = this.context.getManagementServer().queryMBeans(X1ConfigAttributeClass);
      return m != null && m.size() > 0?new AttRef((MOID)m.get(0)):null;
   }

   public String getLabel() {
      return "getLabel called";
   }

   public Boolean getIsCN710() {
      MOID neMOID = MgtUtil.getInstance().getNe(this.moid);
      return Boolean.valueOf(MMUUtil.isCN710(neMOID));
   }

   public Boolean getIsCN810() {
      MOID neMOID = MgtUtil.getInstance().getNe(this.moid);
      return Boolean.valueOf(MMUUtil.isCN810(neMOID));
   }

   public TableRef getGeneralX1ConfigTable() {
      MOID searchMoid = new MOID("com.ericsson.mlne.im.barebone.chassis.Chassis", "*:*", this.resource.getRID());
      MOIDList results = this.context.getManagementServer().queryMBeans(searchMoid);
      if(results != null && results.size() > 0) {
         MOID found = (MOID)results.get(0);
         MOIDList results2 = RelationSearchUtil.getChildren(found, "com.ericsson.mlne.im.barebone.npu.NPU", 2);
         if(results2 != null && results2.size() > 0) {
            MOID table = (MOID)results2.get(0);
            Object o;
            if(MLNEUtil.isTelecomStandard(new Integer[]{Integer.valueOf(2)})) {
               o = this.getAttribute(table, "PhysicalDS1ConfigTable");
            } else {
               o = this.getAttribute(table, "GeneralX1ConfigTable");
            }

            return (TableRef)o;
         }
      }

      return null;
   }

   public String getHelpContext() {
      int telecomStandard = MLNEUtil.getTelecomStandard().value();
      return MLNEUtil.getType().contains(NodeType.CN_810)?"CN810":(MLNEUtil.getType().contains(NodeType.CN_R2) && telecomStandard == 2?"R2_ANSI":(MLNEUtil.getType().contains(NodeType.CN_810_R2)?"CN810_R2":null));
   }
}
