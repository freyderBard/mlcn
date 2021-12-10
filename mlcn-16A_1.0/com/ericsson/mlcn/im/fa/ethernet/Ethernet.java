package com.ericsson.mlcn.im.fa.ethernet;

import com.ericsson.mlcraft.common.tree.ITreeMenuLang;
import com.ericsson.mlcraft.common.util.MLCraftConstants;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.common.util.StaticSnmpIdxHelper;
import com.ericsson.mlcraft.fa.ethernet.status.EthernetServiceStatusGroup;
import com.ericsson.mlcraft.fa.ethernet.status.LanInterfaceStatusGroup;
import com.ericsson.mlcraft.fa.ethernet.status.SwitchPortStatusGroup;
import com.ericsson.mlcraft.fa.ethernet.status.SwitchStatusGroup;
import com.ericsson.mlcraft.fa.ethernet.status.WanInterfaceStatusGroup;
import com.ericsson.mlcraft.im.common.IAttributeGroupHelper;
import com.ericsson.mlcraft.im.common.IAttributeGroupProvider;
import com.ericsson.mlne.im.common.MrBean;
import java.util.List;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxInteger;
import no.axxessit.il.snmp.SnmpAdapter;
import no.axxessit.il.snmp.SnmpResource;
import no.axxessit.il.snmp.SnmpTable;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.RID;

public class Ethernet extends MrBean implements ITreeMenuLang, IAttributeGroupProvider, MLCraftConstants {

   private static final long serialVersionUID = 5677225892692572176L;
   private static final String NPU_LAN_INTERFACE_GROUP = "NPU_LAN_INTERFACE_GROUP";
   private static final String WAN_INTERFACE_GROUP = "WAN_INTERFACE_GROUP";
   private static final String ETHERNET_SERVICE_GROUP = "ETHERNET_SERVICE_GROUP";
   private static final String SWITCH_PORT_GROUP = "SWITCH_PORT_GROUP";
   private static final String SWITCH_GROUP = "SWITCH_GROUP";
   private static final String ATTR_ETHERNET_VARIANT_ENTRY = "xfEthernetServiceEntry";
   private static final String ATTR_ETHERNET_VARIANT = "xfEthernetServiceVariant";
   private static final String TYPE_BRIDGE = "2";
   IAttributeGroupHelper lanStatusGroup = null;
   IAttributeGroupHelper wanStatusGroup = null;
   IAttributeGroupHelper ethernetServiceStatusGroup = null;
   IAttributeGroupHelper switchPortStatusGroup = null;
   IAttributeGroupHelper switchStatusGroup = null;


   public Ethernet(MBeanInfo info) {
      super(info);
   }

   public String getLabel() {
      return TranslationManager.getInstance().getString("$TreeMenu.ETHERNET");
   }

   public MOID getProvider() {
      return this.moid;
   }

   public List getFieldInfo(String id) {
      boolean isEPL_Mode = this.getEPL_ModeSupport();
      return id.equals("NPU_LAN_INTERFACE_GROUP")?this.lanStatusGroup.getFieldInfo():(id.equals("WAN_INTERFACE_GROUP")?this.wanStatusGroup.getFieldInfo():(id.equals("SWITCH_PORT_GROUP") && !isEPL_Mode?this.switchPortStatusGroup.getFieldInfo():(id.equals("SWITCH_GROUP") && !isEPL_Mode?this.switchStatusGroup.getFieldInfo():(id.equals("ETHERNET_SERVICE_GROUP")?this.ethernetServiceStatusGroup.getFieldInfo():null))));
   }

   public synchronized void initStatusGroup(String id, boolean isEPL_Mode) {
      MOID searchMoid;
      if(id.equals("NPU_LAN_INTERFACE_GROUP")) {
         searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet.LanInterface", "LAN:*", this.resource.getRID());
         MOID m = new MOID("com.ericsson.mlne.im.fa.ethernet.LanDcnInterface", "LAN-DCN:*", this.resource.getRID());
         MOIDList m1 = this.context.getManagementServer().queryMBeans(searchMoid);
         m1.addAll(this.context.getManagementServer().queryMBeans(m));
         this.lanStatusGroup = new LanInterfaceStatusGroup(id, m1, this.getContext());
      } else {
         MOIDList m2;
         if(id.equals("WAN_INTERFACE_GROUP")) {
            searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet.WanInterface", "WAN:*", this.resource.getRID());
            m2 = this.context.getManagementServer().queryMBeans(searchMoid);
            this.wanStatusGroup = new WanInterfaceStatusGroup(id, m2, this.getContext());
         } else if(id.equals("SWITCH_PORT_GROUP") && !isEPL_Mode) {
            searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.EthernetSwitchPort", (String)null, this.resource.getRID());
            m2 = this.context.getManagementServer().queryMBeans(searchMoid);
            this.switchPortStatusGroup = new SwitchPortStatusGroup(id, m2, this.getContext());
         } else if(id.equals("SWITCH_GROUP") && !isEPL_Mode) {
            searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.EthernetSwitch", (String)null, this.resource.getRID());
            m2 = this.context.getManagementServer().queryMBeans(searchMoid);
            this.switchStatusGroup = new SwitchStatusGroup(id, m2, this.getContext());
         } else if(id.equals("ETHERNET_SERVICE_GROUP")) {
            searchMoid = new MOID("com.ericsson.mlne.im.fa.ethernet.EplService", (String)null, this.resource.getRID());
            m2 = this.context.getManagementServer().queryMBeans(searchMoid);
            this.ethernetServiceStatusGroup = new EthernetServiceStatusGroup(id, m2, this.getContext());
         }
      }

   }

   public String getTitle(String id) {
      boolean isEPL_Mode = this.getEPL_ModeSupport();
      this.initStatusGroup(id, isEPL_Mode);
      return id.equals("NPU_LAN_INTERFACE_GROUP")?this.lanStatusGroup.getTitle():(id.equals("WAN_INTERFACE_GROUP")?this.wanStatusGroup.getTitle():(id.equals("SWITCH_PORT_GROUP") && !isEPL_Mode?this.switchPortStatusGroup.getTitle():(id.equals("SWITCH_GROUP") && !isEPL_Mode?this.switchStatusGroup.getTitle():(id.equals("ETHERNET_SERVICE_GROUP")?this.ethernetServiceStatusGroup.getTitle():null))));
   }

   public boolean getNumberOfTrafficClassQueuesWritable() {
      if(this.getNPUType().equals("NPU3")) {
         return false;
      } else {
         Integer variant = this.getBridgeVariant();
         if(variant == null) {
            return true;
         } else {
            switch(variant.intValue()) {
            case 2:
               return false;
            default:
               return true;
            }
         }
      }
   }

   private boolean getEPL_ModeSupport() {
      boolean isEPL_Mode = false;
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      MOID npu = RelationSearchUtil.getChild(neMoid, "com.ericsson.mlne.im.barebone.npu.NPU3", 3);
      if(npu != null) {
         Boolean support = (Boolean)this.getAttribute(npu, "EPLModeSupported");
         if(support != null) {
            isEPL_Mode = support.booleanValue();
         }
      }

      return isEPL_Mode;
   }

   public AxxInteger getNumOfTrQueuesRO() {
      return new AxxInteger(8L);
   }

   protected String splitString(String s, char c) {
      String retVal = s;
      if(s != null) {
         int idx = s.lastIndexOf(c);
         if(idx > 0 && idx < s.length()) {
            retVal = s.substring(idx + 1);
         }
      }

      return retVal;
   }

   private Integer getBridgeVariant() {
      SnmpAdapter snmpAdapter = ((SnmpResource)this.resource).getAdapter();
      SnmpTable table = snmpAdapter.getIndexTable("xfEthernetServiceEntry", "xfEthernetServiceVariant", "2", (Object)null);
      String index = "";
      Integer i = null;
      if(table != null) {
         try {
            index = (String)table.getValueAt(0, 0);
         } catch (Exception var6) {
            log.error(var6);
         }
      }

      if(index != "") {
         String str = "xfEthernetServiceVariant." + index;
         i = (Integer)((SnmpResource)this.resource).getAdapter().getAttribute(str, Boolean.valueOf(true));
      }

      return i;
   }

   public String getNPUType() {
      String npuType = null;
      npuType = this.npuClassName();
      npuType = npuType.substring(npuType.lastIndexOf(46) + 1);
      return npuType;
   }

   private String npuClassName() {
      String npuClass = null;
      long npuIndex = this.getNpuIndex();
      MOID searchNpuMoid = new MOID("com.ericsson.mlne.im.common.physical.Module", "*:" + npuIndex, this.resource.getRID());
      MOIDList npuList = this.context.getManagementServer().queryMBeans(searchNpuMoid);
      if(!npuList.isEmpty()) {
         npuClass = ((MOID)npuList.get(0)).getClassname();
      }

      return npuClass;
   }

   private long getNpuIndex() {
      long npuIndex = -1L;
      MOID searchChassisMoid = new MOID("com.ericsson.mlne.im.barebone.chassis.Chassis", "*:" + StaticSnmpIdxHelper.instance().getChassisIdx(), (RID)null);
      MOIDList mList = this.context.getManagementServer().queryMBeans(searchChassisMoid);

      try {
         Long e = (Long)this.context.getManagementServer().getAttribute((MOID)mList.get(0), "NPUSlotPosition");
         AxxEnum chassiType = (AxxEnum)this.context.getManagementServer().getAttribute((MOID)mList.get(0), "Type");
         npuIndex = StaticSnmpIdxHelper.instance().getModuleIdx(e.longValue(), chassiType);
         return npuIndex;
      } catch (MBeanException var7) {
         throw new RuntimeException(var7);
      }
   }
}
