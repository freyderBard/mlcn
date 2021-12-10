package com.ericsson.mlcn.im;

import com.ericsson.mlcn.im.barebone.CFHProgressGroup;
import com.ericsson.mlcn.im.barebone.NEAlarmGroup;
import com.ericsson.mlcn.im.barebone.NEMMUInfoGroup;
import com.ericsson.mlcn.im.barebone.NERAUInfoGroup;
import com.ericsson.mlcn.im.barebone.SFPStatusGroup;
import com.ericsson.mlcraft.app.rl.initialsetup.RLTerminalsGroup;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.FieldInfo;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.LabelOptions;
import com.ericsson.mlcraft.common.status.ActivitiesLoopsGroup;
import com.ericsson.mlcraft.common.status.ActivitiesOAMGroup;
import com.ericsson.mlcraft.common.status.ActivitiesSwUpgradeGroup;
import com.ericsson.mlcraft.common.status.ActivitiesTestsGroup;
import com.ericsson.mlcraft.common.status.X1TestsGroup;
import com.ericsson.mlcraft.common.util.BITS_BitSet;
import com.ericsson.mlcraft.common.util.BitsWrap;
import com.ericsson.mlcraft.common.util.Dialog;
import com.ericsson.mlcraft.common.util.MLNEUtil;
import com.ericsson.mlcraft.common.util.RelationSearchUtil;
import com.ericsson.mlcraft.common.util.StaticSnmpIdxHelper;
import com.ericsson.mlcraft.common.util.Util;
import com.ericsson.mlcraft.im.common.AttributeGroupHelper;
import com.ericsson.mlcraft.im.common.IAttributeGroupHelper;
import com.ericsson.mlcraft.im.common.IAttributeGroupProvider;
import com.ericsson.mlne.im.MLNE;
import com.ericsson.mlne.im.MLNE.MlcWizardTypes;
import com.ericsson.mlne.im.MLNE.NodeType;
import com.ericsson.mlne.im.barebone.NECommonInventoryGroup;
import com.ericsson.mlne.im.barebone.NEHardwareInventoryGroup;
import com.ericsson.mlne.im.barebone.NESoftwareInventoryGroup;
import com.ericsson.mlne.im.common.LicenseAttributes;
import com.ericsson.mlne.im.common.software.upgrade.ModuleProgressGroup;
import com.ericsson.mlne.utils.MMUUtil;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import no.axxessit.client.util.MgtUtil;
import no.axxessit.common.Logger;
import no.axxessit.common.StringUtil;
import no.axxessit.common.as.ServiceLocator;
import no.axxessit.common.lang.TranslationManager;
import no.axxessit.common.msg.EnvironmentException;
import no.axxessit.common.msg.RemoteException;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxEnumConstraint;
import no.axxessit.common.val.AxxString;
import no.axxessit.il.snmp.SnmpAdapter;
import no.axxessit.il.snmp.SnmpProperties;
import no.axxessit.il.snmp.SnmpResource;
import no.axxessit.il.snmp.SnmpTable;
import no.axxessit.im.common.ByteArray;
import no.axxessit.im.common.IPAddress;
import no.axxessit.im.common.TimeZoneDate;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.AttRef;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.AxxMBTableMBean;
import no.axxessit.mgt.MBeanContainer;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MBeanProxy;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;
import no.axxessit.mgt.ManagedResource;
import no.axxessit.mgt.ManagementServer;
import no.axxessit.mgt.OperationException;
import no.axxessit.mgt.RID;
import no.axxessit.mgt.TableRef;
import no.axxessit.mgt.relation.RelationNotFoundException;
import no.axxessit.mgt.relation.RelationResult;
import no.axxessit.mgt.relation.RelationService;

public class MLCN extends MLNE implements LicenseAttributes, IAttributeGroupProvider {

   private static final long serialVersionUID = 6279617394835302127L;
   public static final String[] CN_NPU_CLASSES = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_710", "com.ericsson.mlne.im.barebone.npu.CN_810", "com.ericsson.mlne.im.barebone.npu.CN_210_R2", "com.ericsson.mlne.im.barebone.npu.CN_510_R2", "com.ericsson.mlne.im.barebone.npu.CN_810_R2"};
   private static final String RL_TERMINALS_GROUP_ID = "RL_TERMINALS_GROUP";
   private static final String RL_INITIAL_TERMINALS_GROUP_ID = "RL_INITIAL_TERMINALS_GROUP";
   private static final String ETSI_SYSTEM_TYPE_OID = "1.3.6.1.4.1.193.81.1.1.1";
   private static final String ANSI_SYSTEM_TYPE_OID = "1.3.6.1.4.1.193.81.1.1.2";
   private static final String ETSI_ANSI_SYSTEM_TYPE_OID = "1.3.6.1.4.1.193.81.1.1.3";
   private static final int MLCRAFT_HIGHEST_SEVERITY = 6;
   private static final int POWER_REDANDUNCY_ADMIN_STATUS_ENABLE = 1;
   private static final int POWER_REDANDUNCY_ADMIN_STATUS_DISABLE = 2;
   private static final int CLOCK_INPUT_ENABLE = 1;
   private static final int CLOCK_INPUT_DISABLE = 2;
   private static final String HARDWARE_GROUP_ID = "HARDWARE_GROUP";
   private AttributeGroupHelper hardwareInventorygroup = null;
   private static final String SOFTWARE_GROUP_ID = "SOFTWARE_GROUP";
   private AttributeGroupHelper softwareInventorygroup = null;
   private static final String COMMON_GROUP_ID = "COMMON_GROUP";
   private AttributeGroupHelper commonInventorygroup = null;
   private IAttributeGroupHelper neAlarmGroup = null;
   private IAttributeGroupHelper neRAUMasterInfoGroup = null;
   private IAttributeGroupHelper neMMUMasterInfoGroup = null;
   private IAttributeGroupHelper neRAUSlaveInfoGroup = null;
   private IAttributeGroupHelper neMMUSlaveInfoGroup = null;
   private IAttributeGroupHelper rlTerminalsGroup = null;
   private static final String NE_RAU_MASTER_INFO_GROUP_ID = "NE_RAU_MASTER_INFO_GROUP_ID";
   private static final String NE_MMU_MASTER_INFO_GROUP_ID = "NE_MMU_MASTER_INFO_GROUP_ID";
   private static final String NE_RAU_SLAVE_INFO_GROUP_ID = "NE_RAU_SLAVE_INFO_GROUP_ID";
   private static final String NE_MMU_SLAVE_INFO_GROUP_ID = "NE_MMU_SLAVE_INFO_GROUP_ID";
   private static final String MOID_SEPERATOR = ":";
   private static final String MODULE_PROGRESS_GROUP_ID = "LCN_MLNE_PROGRESS";
   private static final String CFH_PROGRESS_GROUP_ID = "CFH_PROGRESS";
   private IAttributeGroupHelper moduleProgressGroup = null;
   private IAttributeGroupHelper cfhProgressGroup = null;
   private static final int LICENSE_ALARMS_ENABLED = 1;
   private static final int LICENSE_ALARMS_DISABLED = 2;
   private MOID mmuMasterMoid = null;
   private MOID mmuSlaveMoid = null;
   private MOID rmmMasterMoid = null;
   private MOID rmmSlaveMoid = null;
   private static final String SFP_MODULE_GROUP_ID = "SFP_MODULE_GROUP";
   private IAttributeGroupHelper sfpModuleGroup = null;
   protected static final String ATTR_FAU4_NOTIFICATION = "FAU4Notifications";
   protected static final String ATTR_LANCN710_NOTIFICATION = "LanCN710Notifications";
   protected static final String ATTR_MMUCN710_NOTIFICATION = "MMUCN710Notifications";
   protected static final String ATTR_CN710_NOTIFICATION = "CN710Notifications";
   protected static final int EV_ENABLE = 1;
   protected static final int EV_DISABLE = 2;
   private boolean hasSalve = false;
   private static final String ATTR_SUMMARYALARM = "SummaryAlarm";
   private static final int XF_SEVERITY_INDETERMINATE = 1;
   private static HashMap attributeCN710ASMapper = new HashMap();
   protected static Logger log = Logger.getLogger();
   private static TranslationManager tm = TranslationManager.getInstance();
   private static final String CN210_R2_DESCR = "CN 210";
   private static final String CN510_R2_DESCR = "CN 510";
   private static final String CN710_DESCR = "CN 710";
   private static final String CN810_DESCR = "CN 810";
   private static final String CN810_MMU_DESCR = "CN 810 MMU";
   private static final String CN810_R2_DESCR = "CN 810 R2";
   private SnmpAdapter adapter;
   private Map licensedAttributes;
   private static String[] CN_MMUS = new String[]{"com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU2_CN", "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_210", "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_510", "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_710", "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_810"};


   public MLCN(MBeanInfo info) {
      super(info);
      attributeCN710ASMapper.put("PowerFailureLowerInput", new Integer(0));
      attributeCN710ASMapper.put("TrafficFailure", new Integer(1));
      attributeCN710ASMapper.put("ControlFailure", new Integer(2));
      attributeCN710ASMapper.put("InstallationMode", new Integer(3));
      attributeCN710ASMapper.put("LowInputVoltage", new Integer(4));
      attributeCN710ASMapper.put("BidMissing", new Integer(5));
      attributeCN710ASMapper.put("PowerFailureUpperInput", new Integer(6));
      this.licensedAttributes = new HashMap();
   }

   public Boolean getInitialSetupEnabled() {
      SnmpProperties properties = (SnmpProperties)this.snmpResource.getProperties();
      if(properties.getIp().equals("10.0.0.1")) {
         ManagementServer managementServer = this.context.getManagementServer();
         MOIDList chassis = managementServer.queryMBeans(new MOID("com.ericsson.mlne.im.barebone.chassis.Chassis", (String)null, this.resource.getRID()));
         if(chassis.size() == 1) {
            return Boolean.TRUE;
         }
      }

      return Boolean.FALSE;
   }

   public Boolean getConfigurationPresent() {
      Integer present = (Integer)this.snmpResource.getAttribute(this.moid, "ConfigurationPresent");
      return new Boolean((new Integer(2)).equals(present));
   }

   protected AxxMBTableMBean retrieveBoardSoftwareTable() {
      TableRef tableRef = (TableRef)this.getAttribute("BoardSoftwareTable");
      AxxMBTableMBean table = null;

      try {
         table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var4) {
         log.error(var4);
      }

      return table;
   }

   protected AxxMBTableMBean retrieveLoadModuleUpgradeTable() {
      AttRef su = (AttRef)this.getAttribute("SoftwareUpgrade");
      TableRef tableRef = null;

      try {
         tableRef = (TableRef)this.context.getManagementServer().getAttribute(su.getMoid(), "LoadModuleUpgradeTable");
      } catch (OperationException var6) {
         var6.printStackTrace();
      } catch (MBeanException var7) {
         var7.printStackTrace();
      }

      AxxMBTableMBean entries = null;

      try {
         entries = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var5) {
         var5.printStackTrace();
      }

      return entries;
   }

   protected AxxMBTableMBean retrieveSNMPManagerTable() {
      TableRef tableRef = (TableRef)this.getAttribute("SNMPManagerTable");
      AxxMBTableMBean table = null;

      try {
         table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var4) {
         log.error(var4);
      }

      return table;
   }

   protected AxxMBTableMBean retrieveUserInputTable() {
      TableRef tableRef = (TableRef)this.getAttribute("UserInputTable");
      AxxMBTableMBean table = null;

      try {
         table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var4) {
         log.error(var4);
      }

      return table;
   }

   protected AxxMBTableMBean retrieveUserOutputTable() {
      TableRef tableRef = (TableRef)this.getAttribute("UserOutputTable");
      AxxMBTableMBean table = null;

      try {
         table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var4) {
         log.error(var4);
      }

      return table;
   }

   protected AxxMBTableMBean retrieveIPRoutingTable() {
      TableRef tableRef = (TableRef)this.getAttribute("IPRoutingTable");
      AxxMBTableMBean table = null;

      try {
         table = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
      } catch (ClassNotFoundException var4) {
         log.error(var4);
      }

      return table;
   }

   public String getLabel() {
      return "getLabel called";
   }

   public String fetchCachedLabel() {
      return this.getLabel();
   }

   protected MOID getNeMoid() {
      return null;
   }

   public String getStatusMap() {
      String map = null;
      map = "mlcn_ne.map?moid=\\\'" + this.moid.toString() + "\\\'";
      return map;
   }

   public boolean getLicensingSupported() {
      boolean supported = false;

      try {
         String[] e = new String[]{"com.ericsson.mlne.im.barebone.npu.NPU"};
         MOIDList chassis = this.context.getManagementServer().queryMBeans(new MOID("com.ericsson.mlne.im.barebone.chassis.Chassis", (String)null, this.resource.getRID()));
         if(chassis.size() == 1) {
            RelationResult[] result = this.getContext().getRelationService().getReferencedMBeans("*", "parent", (MOID)chassis.get(0), "child", e, 2);
            if(result.length == 1) {
               MOID npu = result[0].getMOID();
               supported = !this.context.getManagementServer().isInstanceOf(npu, "com.ericsson.mlne.im.barebone.npu.NPU_8x2") && !this.context.getManagementServer().isInstanceOf(npu, "com.ericsson.mlne.im.barebone.npu.NPU1_ANSI");
            }
         }
      } catch (RelationNotFoundException var6) {
         log.error("The licensing is not supported if we can\'t find any NPU", var6);
      }

      return supported;
   }

   public AxxEnum getSwGlobalState() {
      ByteArray bits = (ByteArray)this.resourceGetAttribute(this.getId(), "SwGlobalState");
      Hashtable mapper = new Hashtable();
      mapper.put(new BITS_BitSet(new byte[]{(byte)-128}), new Integer(0));
      mapper.put(new BITS_BitSet(new byte[]{(byte)64}), new Integer(1));
      mapper.put(new BITS_BitSet(new byte[]{(byte)32}), new Integer(2));
      mapper.put(new BITS_BitSet(new byte[]{(byte)33}), new Integer(7));
      mapper.put(new BITS_BitSet(new byte[]{(byte)16}), new Integer(3));
      mapper.put(new BITS_BitSet(new byte[]{(byte)17}), new Integer(3));
      mapper.put(new BITS_BitSet(new byte[]{(byte)8}), new Integer(4));
      mapper.put(new BITS_BitSet(new byte[]{(byte)4}), new Integer(5));
      mapper.put(new BITS_BitSet(new byte[]{(byte)5}), new Integer(7));
      mapper.put(new BITS_BitSet(new byte[]{(byte)2}), new Integer(6));
      mapper.put(new BITS_BitSet(new byte[]{(byte)3}), new Integer(6));
      mapper.put(new BITS_BitSet(new byte[]{(byte)1}), new Integer(7));
      mapper.put(new BITS_BitSet(new byte[]{(byte)0, (byte)-128}), new Integer(8));
      mapper.put(new BITS_BitSet(new byte[]{(byte)1, (byte)64}), new Integer(7));

      try {
         int e = ((Integer)mapper.get(new BITS_BitSet(bits))).intValue();
         return new AxxEnum(e, "SW_GLOBAL_STATE");
      } catch (Exception var4) {
         return null;
      }
   }

   public AxxEnum getNotificationStatus() {
      AxxEnum notificationStatus = null;
      Object val = this.resource.getAttribute(this.moid, "NotificationStatus");
      if(val instanceof Integer) {
         Integer longVal = (Integer)val;
         notificationStatus = new AxxEnum(longVal.intValue(), "NOTIFICATION_STATUS");
      } else if(val instanceof Long) {
         Long longVal1 = (Long)val;
         notificationStatus = new AxxEnum(longVal1.intValue(), "NOTIFICATION_STATUS");
      }

      return notificationStatus;
   }

   public AxxEnum getSystemType() {
      RID rid = this.resource.getRID();
      MOID moid = new MOID("com.ericsson.mlcn.im.MLCN", "eqroot", rid);
      AxxEnum systemType = null;
      Object obj = this.resource.getAttribute(moid, "SystemType");
      String s = obj.toString();
      if(s.equals("1.3.6.1.4.1.193.81.1.1.1")) {
         systemType = new AxxEnum(1, "SYSTEM_TYPE");
      } else if(s.equals("1.3.6.1.4.1.193.81.1.1.2")) {
         systemType = new AxxEnum(2, "SYSTEM_TYPE");
      } else if(s.equals("1.3.6.1.4.1.193.81.1.1.3")) {
         Integer telecomStandard = (Integer)this.getContext().getResource().getAttribute(moid, "TelecomStandard");
         systemType = new AxxEnum(telecomStandard.intValue(), "SYSTEM_TYPE");
      } else {
         log.warn("Method " + this.getId().getClassname() + ": getSystemType() + failed to read MO or received unexpected value");
      }

      return systemType;
   }

   public String getPMStartTime() {
      String pmStartTime = null;
      String tmp = (String)this.resource.getAttribute(this.moid, "PMStartTime");
      byte[] tmp2 = tmp.getBytes();
      Calendar c = Calendar.getInstance();
      c.set(11, tmp2[0]);
      c.set(12, tmp2[1]);
      SimpleDateFormat pmStartTimeFormat = new SimpleDateFormat(tm.getString("$MLCraftCommon.TIME_FORMAT_SHORT"));
      pmStartTime = pmStartTimeFormat.format(new Date(c.getTimeInMillis()));
      return pmStartTime;
   }

   public void setPMStartTime(String newPMStartTime) {
      byte[] pmStartTime = new byte[]{(byte)0, (byte)0, (byte)0};
      SimpleDateFormat formatter = new SimpleDateFormat(tm.getString("$MLCraftCommon.TIME_FORMAT_SHORT"));
      ParsePosition dummy = new ParsePosition(0);
      Calendar c = Calendar.getInstance();
      c.setTime(formatter.parse(newPMStartTime, dummy));
      Integer hours = new Integer(c.get(11));
      Integer minutes = new Integer(c.get(12));
      pmStartTime[0] = hours.byteValue();
      pmStartTime[1] = minutes.byteValue();
      this.resource.setAttribute(this.moid, "PMStartTime", new String(pmStartTime));
   }

   public boolean getHideLicenseWarnings() {
      boolean hideLicenseWarnings = false;
      Integer licAlarmWarnings = (Integer)this.resource.getAttribute(this.moid, "HideLicenseWarnings");
      hideLicenseWarnings = licAlarmWarnings.intValue() == 2;
      return hideLicenseWarnings;
   }

   public void setHideLicenseWarnings(boolean newHideLicenseWarnings) {
      this.resource.setAttribute(this.moid, "HideLicenseWarnings", new Integer(newHideLicenseWarnings?2:1));
   }

   public boolean getHideLicenseErrors() {
      boolean hideLicenseErrors = false;
      Integer licAlarmErrors = (Integer)this.resource.getAttribute(this.moid, "HideLicenseErrors");
      hideLicenseErrors = licAlarmErrors.intValue() == 2;
      return hideLicenseErrors;
   }

   public void setHideLicenseErrors(boolean newHideLicenseErrors) {
      this.resource.setAttribute(this.moid, "HideLicenseErrors", new Integer(newHideLicenseErrors?2:1));
   }

   public boolean getLocalTimeSupported() {
      boolean localTimeSupported = false;
      Object timeContinent = null;
      timeContinent = this.resource.getAttribute(this.moid, "TimeContinent");
      localTimeSupported = timeContinent != null;
      return localTimeSupported;
   }

   public void setSystemClock(Date newSystemClock) {
      byte maxRetries = 3;
      byte sleepNoOfSec = 1;
      int nrOfTry = 0;

      boolean keepTrying;
      do {
         keepTrying = false;

         try {
            this.resource.setAttribute(this.moid, "SystemClock", newSystemClock);
         } catch (RemoteException var9) {
            try {
               Util.sleepAtLeast((long)(1001 * sleepNoOfSec));
            } catch (InterruptedException var8) {
               log.error(var8);
            }

            keepTrying = true;
         }

         ++nrOfTry;
      } while(nrOfTry <= maxRetries && keepTrying);

      if(keepTrying) {
         TranslationManager tm = TranslationManager.getInstance();
         Dialog.showDialog(tm.getString("$MLCraftCommon.CONFIGURE_BASIC_NE_FAILED_TO_SAVE_TIME"));
      }

   }

   public String getTimeZone() {
      String timeZone = null;
      Date time = (Date)this.resource.getAttribute(this.moid, "SystemClock");
      if(time instanceof TimeZoneDate) {
         TimeZoneDate continent = (TimeZoneDate)time;
         if(continent.getTimeZone() != null) {
            timeZone = continent.getTimeZone();
         }
      }

      if(timeZone == null) {
         String continent1 = (String)this.resource.getAttribute(this.moid, "TimeContinent");
         String city = (String)this.resource.getAttribute(this.moid, "TimeCity");
         if(continent1 == null || continent1.equals("") || city == null || city.equals("")) {
            return "";
         }

         Calendar c = Calendar.getInstance();
         c.setTimeZone(TimeZone.getTimeZone(continent1 + "/" + city));
         int offSet = c.getTimeZone().getRawOffset() / 3600000;
         if(continent1.equalsIgnoreCase("etc")) {
            offSet *= -1;
         }

         c = Calendar.getInstance();
         c.set(11, offSet < 0?offSet * -1:offSet);
         c.set(12, 0);
         Object[] arg = new Object[]{offSet >= 0?new Character('+'):new Character('-'), new Date(c.getTimeInMillis())};
         timeZone = MessageFormat.format(tm.getString("$MLCraftCommon.UTC_DEVIATION_FORMAT"), arg);
      }

      return timeZone;
   }

   public Date getSystemClock() {
      return (Date)this.resource.getAttribute(this.moid, "SystemClock");
   }

   public String getSystemClockRO() {
      String dtFormat = "yyyy-MM-dd HH:mm:ss";
      if(Locale.US.equals(Locale.getDefault())) {
         dtFormat = "MM-dd-yyyy HH:mm:ss";
      }

      Date time = (Date)this.resource.getAttribute(this.moid, "SystemClock");
      int year = time.getYear() + 1900;
      int month = time.getMonth();
      int date = time.getDate();
      int hour = time.getHours();
      int minutes = time.getMinutes();
      int seconds = time.getSeconds();
      Calendar c = Calendar.getInstance();
      c.set(1, year);
      c.set(2, month);
      c.set(5, date);
      c.set(11, hour);
      c.set(12, minutes);
      c.set(13, seconds);
      SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);
      String timeString = sdf.format(c.getTime());
      return timeString;
   }

   public Boolean getSendAlarmsAndEvents() {
      Boolean sendAlarmsAndEvents = Boolean.valueOf(true);
      Object val = this.resource.getAttribute(this.moid, "SendAlarmsAndEvents");
      if(val instanceof Integer) {
         Integer longVal = (Integer)val;
         sendAlarmsAndEvents = Boolean.valueOf(longVal.intValue() == 1);
      } else if(val instanceof Long) {
         Long longVal1 = (Long)val;
         sendAlarmsAndEvents = Boolean.valueOf(longVal1.intValue() == 1);
      }

      return sendAlarmsAndEvents;
   }

   public void setSendAlarmsAndEvents(Boolean newSendAlarmsAndEvents) {
      int sendAlarmsAndEvents = newSendAlarmsAndEvents.booleanValue()?1:2;
      this.resource.setAttribute(this.moid, "SendAlarmsAndEvents", new Integer(sendAlarmsAndEvents));
   }

   public void setDcnNTPAddress(AxxString newDcnNTPAddress) {
      String dcnNTPAddress = newDcnNTPAddress.value();
      if(dcnNTPAddress.equals("")) {
         dcnNTPAddress = "0.0.0.0";
      }

      this.resource.setAttribute(this.moid, "DcnNTPAddress", dcnNTPAddress);
   }

   public void setDHCPAddress(String newDHCPAddress) {
      String dhcpAddress = newDHCPAddress;
      if(newDHCPAddress.equals("")) {
         dhcpAddress = "0.0.0.0";
      }

      this.resource.setAttribute(this.moid, "DHCPAddress", dhcpAddress);
   }

   public String getUserName() {
      this.snmpResource = (SnmpResource)this.resource;
      SnmpProperties prop = (SnmpProperties)this.snmpResource.getProperties();
      return prop.getUser();
   }

   public Map retrieveLicenseAttributes() {
      this.loadUserIOLicenses();
      return this.licensedAttributes;
   }

   private void loadUserIOLicenses() {
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      ArrayList licenseInput;
      if(MMUUtil.isCN510_R2(neMoid).booleanValue()) {
         licenseInput = new ArrayList();
         licenseInput.add("xfAuxInEnable");
         this.licensedAttributes.put("UserInputTable", licenseInput);
      }

      if(MMUUtil.isCN510_R2(neMoid).booleanValue() || MMUUtil.isCN810(neMoid) || MMUUtil.isCN810_R2(neMoid)) {
         licenseInput = new ArrayList();
         licenseInput.add("xfAuxOutEnable");
         this.licensedAttributes.put("UserOutputTable", licenseInput);
      }

   }

   public MOID getProvider() {
      return this.moid;
   }

   public List getFieldInfo(String id) {
      if(id.equals("NE_ALARM_GROUP_ID")) {
         return this.neAlarmGroup.getFieldInfo();
      } else if(id.equals("NE_RAU_MASTER_INFO_GROUP_ID")) {
         return this.neRAUMasterInfoGroup.getFieldInfo();
      } else if(id.equals("NE_RAU_SLAVE_INFO_GROUP_ID")) {
         return this.neRAUSlaveInfoGroup != null?this.neRAUSlaveInfoGroup.getFieldInfo():null;
      } else if(id.equals("NE_MMU_MASTER_INFO_GROUP_ID")) {
         return this.neMMUMasterInfoGroup.getFieldInfo();
      } else if(id.equals("NE_MMU_SLAVE_INFO_GROUP_ID")) {
         return this.neMMUSlaveInfoGroup != null?this.neMMUSlaveInfoGroup.getFieldInfo():null;
      } else if(!id.equals("RL_TERMINALS_GROUP") && !id.equals("RL_INITIAL_TERMINALS_GROUP")) {
         if(id.equals("LCN_MLNE_PROGRESS")) {
            return this.moduleProgressGroup.getFieldInfo();
         } else if(id.equals("CFH_PROGRESS")) {
            return this.cfhProgressGroup.getFieldInfo();
         } else if(id.equals("SFP_MODULE_GROUP")) {
            return this.sfpModuleGroup.getFieldInfo();
         } else if(id.equals("X1_RUNNING_TESTS_GROUP")) {
            return this.x1RunningTestsGroup.getFieldInfo();
         } else if(id.equals("X1_RECENT_TESTS_GROUP")) {
            return this.x1RecentTestsGroup.getFieldInfo();
         } else if(id.equals("ACTIVITIES_SW_UPGRADE_GROUP")) {
            return this.activitiesSwUpgradeGroup.getFieldInfo();
         } else if(id.equals("ACTIVITIES_LOOPS_GROUP")) {
            return this.activitiesLoopsGroup.getFieldInfo(false);
         } else if(id.equals("ACTIVITIES_TESTS_GROUP")) {
            return this.activitiesTestsGroup.getFieldInfo(false);
         } else if(id.equals("ACTIVITIES_OAM_GROUP") && this.activitiesOAMGroup.linkOamSupported) {
            return this.activitiesOAMGroup.getFieldInfo();
         } else {
            ArrayList fieldInfo;
            LabelOptions labelOptions;
            if(id.equals("ACTIVITIES_LOOPS_TEXT_GROUP")) {
               fieldInfo = new ArrayList();
               labelOptions = new LabelOptions(false, 0, 1.0D, (String)null);
               fieldInfo.add(new FieldInfo(FieldInfo.LABEL, this.activitiesLoopsGroup.getText(), labelOptions));
               return fieldInfo;
            } else if(id.equals("ACTIVITIES_TESTS_TEXT_GROUP")) {
               fieldInfo = new ArrayList();
               labelOptions = new LabelOptions(false, 0, 1.0D, (String)null);
               fieldInfo.add(new FieldInfo(FieldInfo.LABEL, this.activitiesTestsGroup.getText(), labelOptions));
               return fieldInfo;
            } else {
               return "HARDWARE_GROUP".equals(id)?this.hardwareInventorygroup.getFieldInfo():("SOFTWARE_GROUP".equals(id)?this.softwareInventorygroup.getFieldInfo():("COMMON_GROUP".equals(id)?this.commonInventorygroup.getFieldInfo():null));
            }
         }
      } else {
         return this.rlTerminalsGroup.getFieldInfo();
      }
   }

   public String getTitle(String id) {
      this.initAttributeGroup(id);
      if(id.equals("NE_ALARM_GROUP_ID")) {
         return this.neAlarmGroup.getTitle();
      } else if(id.equals("NE_RAU_MASTER_INFO_GROUP_ID")) {
         return this.getRAUMasterTitle();
      } else {
         if(id.equals("NE_RAU_SLAVE_INFO_GROUP_ID")) {
            if(this.neRAUSlaveInfoGroup != null) {
               return this.getRAUSlaveTitle();
            }
         } else {
            if(id.equals("NE_MMU_MASTER_INFO_GROUP_ID")) {
               return this.getMMUMasterTitle();
            }

            if(id.equals("NE_MMU_SLAVE_INFO_GROUP_ID")) {
               return this.neMMUSlaveInfoGroup != null?this.getMMUSlaveTitle():"";
            }

            if(id.equals("RL_TERMINALS_GROUP") || id.equals("RL_INITIAL_TERMINALS_GROUP")) {
               return this.rlTerminalsGroup.getTitle();
            }

            if(id.equals("SFP_MODULE_GROUP")) {
               return this.sfpModuleGroup.getTitle();
            }

            if(id.equals("X1_RUNNING_TESTS_GROUP")) {
               return this.x1RunningTestsGroup.getTitle();
            }

            if(id.equals("X1_RECENT_TESTS_GROUP")) {
               return this.x1RecentTestsGroup.getTitle();
            }

            if(id.equals("ACTIVITIES_SW_UPGRADE_GROUP")) {
               return this.activitiesSwUpgradeGroup.getTitle();
            }

            if(id.equals("ACTIVITIES_LOOPS_GROUP")) {
               return this.activitiesLoopsGroup.getTitle();
            }

            if(id.equals("ACTIVITIES_TESTS_GROUP")) {
               return this.activitiesTestsGroup.getTitle();
            }

            if(id.equals("ACTIVITIES_OAM_GROUP") && this.activitiesOAMGroup.linkOamSupported) {
               return this.activitiesOAMGroup.getTitle();
            }

            if(id.equals("ACTIVITIES_LOOPS_TEXT_GROUP")) {
               return null;
            }

            if(id.equals("ACTIVITIES_TESTS_TEXT_GROUP")) {
               return null;
            }

            if("HARDWARE_GROUP".equals(id)) {
               return this.hardwareInventorygroup.getTitle();
            }

            if("SOFTWARE_GROUP".equals(id)) {
               return this.softwareInventorygroup.getTitle();
            }

            if("COMMON_GROUP".equals(id)) {
               return this.commonInventorygroup.getTitle();
            }
         }

         return null;
      }
   }

   private String getMMUSlaveTitle() {
      String title = this.neMMUSlaveInfoGroup.getTitle();
      if(this.getIsCN710().booleanValue()) {
         return "CN 710 MMU";
      } else {
         if(this.getIsCN210_R2().booleanValue()) {
            title = "CN 210";
         } else if(this.getIsCN510_R2().booleanValue()) {
            title = "CN 510";
         } else if(this.getIsCN810().booleanValue() || this.getIsCN810_R2().booleanValue()) {
            return "CN 810 MMU";
         }

         if(this.getMMUSlave() != null) {
            title = title + " Slave";
         }

         return title;
      }
   }

   private String getMMUMasterTitle() {
      String title = this.neMMUMasterInfoGroup.getTitle();
      if(this.getIsCN710().booleanValue()) {
         return "CN 710";
      } else {
         if(this.getIsCN210_R2().booleanValue()) {
            title = "CN 210";
         } else if(this.getIsCN510_R2().booleanValue()) {
            title = "CN 510";
         } else {
            if(this.getIsCN810().booleanValue()) {
               return "CN 810";
            }

            if(this.getIsCN810_R2().booleanValue()) {
               return "CN 810 R2";
            }
         }

         if(this.getMMUSlave() != null) {
            title = title + " Master";
         }

         return title;
      }
   }

   private String getRAUSlaveTitle() {
      String title = this.neRAUSlaveInfoGroup.getTitle();
      if(!this.getIsCN710().booleanValue() && !this.getIsCN810().booleanValue() && !this.getIsCN810_R2().booleanValue()) {
         if(this.getRAUSlave() != null) {
            title = title + " Slave";
         }

         return title;
      } else {
         return title;
      }
   }

   private String getRAUMasterTitle() {
      String title = this.neRAUMasterInfoGroup.getTitle();
      if(!this.getIsCN710().booleanValue() && !this.getIsCN810().booleanValue() && !this.getIsCN810_R2().booleanValue()) {
         if(this.getRAUSlave() != null) {
            title = title + " Master";
         }

         return title;
      } else {
         return title;
      }
   }

   public synchronized void initAttributeGroup(String id) {
      MOIDList m = new MOIDList();
      if(id.equals("NE_ALARM_GROUP_ID")) {
         this.resolveNeAlarmGroup(id);
      } else if(id.equals("NE_MMU_MASTER_INFO_GROUP_ID")) {
         this.resolveNeMMUMasterInfoGroup(id);
      } else if(id.equals("NE_MMU_SLAVE_INFO_GROUP_ID")) {
         this.resolveNEMMUInfoGroup(id);
      } else if(id.equals("NE_RAU_MASTER_INFO_GROUP_ID")) {
         this.resolveNERAUInfoGroup(id);
      } else if(id.equals("NE_RAU_SLAVE_INFO_GROUP_ID")) {
         this.resolveNERAUSlaveInfoGroup(id);
      } else if(id.equals("RL_TERMINALS_GROUP")) {
         this.rlTerminalsGroup = new RLTerminalsGroup(id, this.getContext(), true, false);
      } else if(id.equals("RL_INITIAL_TERMINALS_GROUP")) {
         this.rlTerminalsGroup = new RLTerminalsGroup(id, this.getContext(), true, true);
      } else if(id.equals("LCN_MLNE_PROGRESS")) {
         AxxMBTableMBean moidList = this.retrieveLoadModuleUpgradeTable();
         if(moidList != null) {
            MOID[] moids = moidList.getRows();
            MOIDList moidList1 = new MOIDList(moids);
            this.moduleProgressGroup = new ModuleProgressGroup(id, moidList1, this.getContext());
         }
      } else if(id.equals("CFH_PROGRESS")) {
         this.resolveCFHGrogressInfoGroup(id);
      } else if(id.equals("SFP_MODULE_GROUP")) {
         MOIDList moidList2 = new MOIDList();
         moidList2.add(this.moid);
         this.sfpModuleGroup = new SFPStatusGroup(id, moidList2, this.getContext());
      } else if(id.equals("X1_RUNNING_TESTS_GROUP")) {
         this.x1RunningTestsGroup = new X1TestsGroup(id, 1, "Running tests on NE", this.getTestStartedInterfaces(), this.getContext());
      } else if(id.equals("X1_RECENT_TESTS_GROUP")) {
         this.x1RecentTestsGroup = new X1TestsGroup(id, 2, "Test Result", this.getRecentTestInterfaces(), this.getContext());
      } else if(id.equals("ACTIVITIES_SW_UPGRADE_GROUP")) {
         m.add(this.moid);
         this.activitiesSwUpgradeGroup = new ActivitiesSwUpgradeGroup(id, m, this.context);
      } else if(!id.equals("ACTIVITIES_LOOPS_GROUP") && !id.equals("ACTIVITIES_LOOPS_TEXT_GROUP")) {
         if(!id.equals("ACTIVITIES_TESTS_GROUP") && !id.equals("ACTIVITIES_TESTS_TEXT_GROUP")) {
            if(id.equals("ACTIVITIES_OAM_GROUP")) {
               m.add(this.moid);
               this.activitiesOAMGroup = new ActivitiesOAMGroup(id, m, this.context);
            } else if("HARDWARE_GROUP".equals(id)) {
               m.add(this.moid);
               this.hardwareInventorygroup = new NEHardwareInventoryGroup(id, "Hardware Inventory", m, this.getContext(), this.snmpResource);
            } else if("SOFTWARE_GROUP".equals(id)) {
               m.add(this.moid);
               this.softwareInventorygroup = new NESoftwareInventoryGroup(id, "Software Inventory", m, this.getContext(), this.snmpResource);
            } else if("COMMON_GROUP".equals(id)) {
               m.add(this.moid);
               this.commonInventorygroup = new NECommonInventoryGroup(id, "Common Inventory", m, this.getContext(), this.snmpResource);
            }
         } else {
            m.add(this.moid);
            this.activitiesTestsGroup = new ActivitiesTestsGroup(id, m, this.context);
         }
      } else {
         m.add(this.moid);
         this.activitiesLoopsGroup = new ActivitiesLoopsGroup(id, m, this.context);
      }

   }

   private void resolveCFHGrogressInfoGroup(String id) {
      if(this.cfhProgressGroup == null) {
         MOIDList moidList = new MOIDList();
         moidList.add(this.moid);
         this.cfhProgressGroup = new CFHProgressGroup(id, moidList, this.getContext());
      }

   }

   private void resolveNERAUSlaveInfoGroup(String id) {
      if(this.neRAUSlaveInfoGroup == null && this.getMMUSlave() != null) {
         MOIDList RAUSlaveMoidList = new MOIDList();
         RAUSlaveMoidList.add(this.getRAUSlave());
         RAUSlaveMoidList.add(this.moid);
         this.neRAUSlaveInfoGroup = new NERAUInfoGroup(id, RAUSlaveMoidList, this.getContext(), Boolean.valueOf(false));
      }

   }

   private void resolveNERAUInfoGroup(String id) {
      if(this.neRAUMasterInfoGroup == null) {
         MOIDList RAUMasterMoidList = new MOIDList();
         RAUMasterMoidList.add(this.getRAUMaster());
         RAUMasterMoidList.add(this.moid);
         this.neRAUMasterInfoGroup = new NERAUInfoGroup(id, RAUMasterMoidList, this.getContext(), Boolean.valueOf(true));
      }

   }

   private void resolveNEMMUInfoGroup(String id) {
      if(this.neMMUSlaveInfoGroup == null && this.getMMUSlave() != null) {
         MOIDList MMUSlaveMoidList = new MOIDList();
         MMUSlaveMoidList.add(this.getMMUSlave());
         MMUSlaveMoidList.add(this.moid);
         this.neMMUSlaveInfoGroup = new NEMMUInfoGroup(id, MMUSlaveMoidList, this.getContext(), Boolean.valueOf(false));
      }

   }

   private void resolveNeMMUMasterInfoGroup(String id) {
      if(this.neMMUMasterInfoGroup == null) {
         MOIDList MMUMasterMoidList = new MOIDList();
         MMUMasterMoidList.add(this.getMMUMaster());
         MMUMasterMoidList.add(this.moid);
         this.neMMUMasterInfoGroup = new NEMMUInfoGroup(id, MMUMasterMoidList, this.getContext(), Boolean.valueOf(true));
      }

   }

   private void resolveNeAlarmGroup(String id) {
      if(this.neAlarmGroup == null || this.hasSalve ^ this.getHasSlave().booleanValue()) {
         this.hasSalve = this.getHasSlave().booleanValue();
         MOIDList m = new MOIDList();
         m.add(this.moid);
         this.neAlarmGroup = new NEAlarmGroup(id, m, this.getContext(), this.getHasSlave().booleanValue(), this.getIsProtectionSupported().booleanValue());
      }

   }

   public MOID getRAUMaster() {
      MOID mmuMoid = this.getMMUMaster();
      if(mmuMoid == null) {
         return null;
      } else {
         MOID rauMasterMoid = RelationSearchUtil.getChild(mmuMoid, "com.ericsson.mlne.im.app.rl.common.physical.rau.RAU", 2);
         return rauMasterMoid;
      }
   }

   public MOID getRAUSlave() {
      MOID mmuMoid = this.getMMUSlave();
      if(mmuMoid == null) {
         return null;
      } else {
         MOID rauSlaveMoid = RelationSearchUtil.getChild(mmuMoid, "com.ericsson.mlne.im.app.rl.common.physical.rau.RAU", 2);
         return rauSlaveMoid;
      }
   }

   public MOID getMMUMaster() {
      if(this.mmuMasterMoid == null) {
         MOID neMoid = IMUtil.getInstance().getNe(new MOID((String)null, "eqroot", this.moid.getResourceId()));
         MOIDList mList = RelationSearchUtil.getChildren(neMoid, "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU2_CN", 3);
         if(mList.isEmpty()) {
            mList = RelationSearchUtil.getChildren(neMoid, "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_710", 3);
            if(mList.isEmpty()) {
               mList = RelationSearchUtil.getChildren(neMoid, "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_510", 3);
            }

            if(mList.isEmpty()) {
               mList = RelationSearchUtil.getChildren(neMoid, "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_210", 3);
            }

            if(mList.isEmpty()) {
               mList = RelationSearchUtil.getChildren(neMoid, "com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_810", 3);
            }
         }

         String index = "";
         long slotnr = 0L;
         long idx = 0L;
         Iterator i$ = mList.iterator();

         while(i$.hasNext()) {
            MOID mmumoid = (MOID)i$.next();
            index = mmumoid.getObjectId().getId();
            index = index.substring(index.lastIndexOf(":") + 1);
            idx = Long.parseLong(index);
            slotnr = StaticSnmpIdxHelper.instance().getSlotNo(idx);
            if(slotnr != 2L && slotnr != 1L) {
               if(slotnr == 3L || slotnr == 0L) {
                  this.mmuSlaveMoid = mmumoid;
               }
            } else {
               this.mmuMasterMoid = mmumoid;
            }
         }
      }

      return this.mmuMasterMoid;
   }

   public Boolean getHasSlave() {
      return Boolean.valueOf(this.getMMUSlave() != null);
   }

   public MOID getMMUSlave() {
      boolean slaveMoidFound = false;
      MOID neMoid = IMUtil.getInstance().getNe(new MOID((String)null, "eqroot", this.moid.getResourceId()));
      MOIDList mList = RelationSearchUtil.getChildren(neMoid, CN_MMUS, 3);
      if(MLNEUtil.isType(new NodeType[]{NodeType.CN_710}) && mList.size() == 1) {
         String[] index = new String[]{"com.ericsson.mlne.im.common.physical.Module"};
         RelationResult[] slotnr = null;
         RelationService service = MgtUtil.getInstance().getRelationService();

         try {
            slotnr = service.getReferencedMBeans("*", "parent", neMoid, "child", new String[]{"com.ericsson.mlne.im.barebone.chassis.Chassis"}, 1);
            RelationResult[] idx = service.getReferencedMBeans("AXXRelation", "parent", slotnr[0].getMOID(), "child", index, 2);

            for(int i = 0; i < idx.length; ++i) {
               String i$ = idx[i].getMOID().getObjectId().getId();
               if(i$.startsWith("Unsupported")) {
                  mList.add(idx[i].getMOID());
               }
            }
         } catch (RelationNotFoundException var11) {
            log.error("RelationNotFoundException in getMMUSlave");
         }
      }

      String var12 = "";
      long var13 = 0L;
      long var14 = 0L;
      Iterator var15 = mList.iterator();

      while(var15.hasNext()) {
         MOID mmumoid = (MOID)var15.next();
         var12 = mmumoid.getObjectId().getId();
         var12 = var12.substring(var12.lastIndexOf(":") + 1);
         var14 = Long.parseLong(var12);
         var13 = StaticSnmpIdxHelper.instance().getSlotNo(var14);
         if(var13 == 2L) {
            this.mmuMasterMoid = mmumoid;
         } else if(var13 == 3L) {
            this.mmuSlaveMoid = mmumoid;
            slaveMoidFound = true;
         }
      }

      if(slaveMoidFound) {
         return this.mmuSlaveMoid;
      } else {
         return null;
      }
   }

   private MOID getRMMMaster() {
      if(this.rmmMasterMoid == null) {
         String[] toClasses = new String[]{"com.ericsson.mlne.im.barebone.npu.RMM"};
         MOIDList mList = RelationSearchUtil.getChildren(this.moid, toClasses, 5);
         String index = "";
         long slotnr = 0L;
         long idx = 0L;
         Iterator i$ = mList.iterator();

         while(i$.hasNext()) {
            MOID rmmmoid = (MOID)i$.next();
            index = rmmmoid.getObjectId().getId();
            index = index.substring(index.lastIndexOf(":") + 1);
            idx = Long.parseLong(index);
            slotnr = StaticSnmpIdxHelper.instance().getSlotNo(idx);
            if(!this.getIsCN710().booleanValue() && !this.getIsCN810().booleanValue() && !this.getIsCN810_R2().booleanValue()) {
               if(this.getIsCNX10_R2().booleanValue()) {
                  if(slotnr == 1L) {
                     this.rmmMasterMoid = rmmmoid;
                  } else if(slotnr == 2L) {
                     this.rmmSlaveMoid = rmmmoid;
                  }
               } else if(slotnr == 2L) {
                  this.rmmMasterMoid = rmmmoid;
               } else if(slotnr == 3L) {
                  this.rmmSlaveMoid = rmmmoid;
               }
            } else if(slotnr == 1L) {
               this.rmmMasterMoid = rmmmoid;
            }
         }
      }

      return this.rmmMasterMoid;
   }

   private MOID getRMMSlave() {
      if(this.rmmSlaveMoid == null) {
         String[] toClasses = new String[]{"com.ericsson.mlne.im.barebone.npu.RMM"};
         MOIDList mList = RelationSearchUtil.getChildren(this.moid, toClasses, 5);
         String index = "";
         long slotnr = 0L;
         long idx = 0L;
         Iterator i$ = mList.iterator();

         while(i$.hasNext()) {
            MOID rmmmoid = (MOID)i$.next();
            index = rmmmoid.getObjectId().getId();
            index = index.substring(index.lastIndexOf(":") + 1);
            idx = Long.parseLong(index);
            slotnr = StaticSnmpIdxHelper.instance().getSlotNo(idx);
            if(slotnr == 2L) {
               this.rmmMasterMoid = rmmmoid;
            } else if(slotnr == 3L) {
               this.rmmSlaveMoid = rmmmoid;
            }
         }
      }

      return this.rmmSlaveMoid;
   }

   public String getRMMHWRevMaster() {
      MOID rmm = this.getRMMMaster();
      return this.fetchHWRev(rmm);
   }

   public String getCNSWRevMaster() {
      if(MLNEUtil.isType(new NodeType[]{NodeType.CN_R0, NodeType.CN_R1})) {
         return this.getMMUSWRevMaster();
      } else {
         MOID nPU = this.getNpu(false);
         return this.fetchSWRev(nPU);
      }
   }

   public String getCNSWRevSlave() {
      return MLNEUtil.isType(new NodeType[]{NodeType.CN_R0, NodeType.CN_R1})?this.getMMUSWRevSlave():this.fetchSWRev(this.getNpu(true));
   }

   public String getRMMHWRevSlave() {
      MOID rmmSlaveMoid = this.getRMMSlave();
      return this.fetchHWRev(rmmSlaveMoid);
   }

   public String getMMUSWSBLRevMaster() {
      TableRef tableRef = null;
      AxxMBTableMBean entries = null;
      String revision = null;
      Integer activeRelease = Integer.valueOf(-1);

      try {
         AttRef e = (AttRef)this.getAttribute("SoftwareUpgrade");
         activeRelease = (Integer)this.context.getManagementServer().getAttribute(e.getMoid(), "ActiveRelease");
         tableRef = (TableRef)this.context.getManagementServer().getAttribute(e.getMoid(), "SwReleaseTable");
         entries = (AxxMBTableMBean)MBeanProxy.createInstance(tableRef.getMoid(), this.context.getManagementServer());
         if(entries != null) {
            MOID[] moids = entries.getRows();
            if(activeRelease.intValue() <= moids.length) {
               revision = (String)this.context.getManagementServer().getAttribute(moids[activeRelease.intValue() - 1], "Revision");
            } else {
               revision = "N/A";
            }
         }
      } catch (OperationException var7) {
         log.error(var7);
      } catch (MBeanException var8) {
         log.error(var8);
      } catch (ClassNotFoundException var9) {
         log.error(var9);
      }

      if(revision.lastIndexOf("_") != -1) {
         revision = revision.substring(revision.lastIndexOf("_") + 1);
      }

      return revision;
   }

   public String getMMUHWRevMaster() {
      MOID mmuMasterMoid = this.getMMUMaster();
      return this.fetchHWRev(mmuMasterMoid);
   }

   public String getMMUHWRevSlave() {
      MOID mmuSlaveMoid = this.getMMUSlave();
      return this.fetchHWRev(mmuSlaveMoid);
   }

   public String getMMUSWRevMaster() {
      MOID mmuMasterMoid = this.getMMUMaster();
      return this.fetchSWRev(mmuMasterMoid);
   }

   public String getMMUSWRevSlave() {
      MOID mmuSlaveMoid = this.getMMUSlave();
      return this.fetchSWRev(mmuSlaveMoid);
   }

   public String getRAUHWRevMaster() {
      MOID rauMasterMoid = this.getRAUMaster();
      return this.fetchHWRev(rauMasterMoid);
   }

   public String getRAUHWRevSlave() {
      MOID rauSlaveMoid = this.getRAUSlave();
      return this.fetchHWRev(rauSlaveMoid);
   }

   public String getRAUSWRevMaster() {
      MOID rauMasterMoid = this.getRAUMaster();
      return this.fetchSWRev(rauMasterMoid);
   }

   public String getRAUSWRevSlave() {
      MOID rauSlaveMoid = this.getRAUSlave();
      return this.fetchSWRev(rauSlaveMoid);
   }

   public String getCNHWRevMaster() {
      if(MLNEUtil.isType(new NodeType[]{NodeType.CN_R0, NodeType.CN_R1})) {
         return this.getMMUHWRevMaster();
      } else {
         MOID npu = this.getNpu(false);
         return this.fetchHWRev(npu);
      }
   }

   public String getCNHWRevSlave() {
      if(MLNEUtil.isType(new NodeType[]{NodeType.CN_R0, NodeType.CN_R1})) {
         return this.getMMUHWRevSlave();
      } else {
         MOID nPU = this.getNpu(true);
         return this.fetchHWRev(nPU);
      }
   }

   private MOID getNpu(boolean isSlave) {
      MOIDList cns = RelationSearchUtil.getChildren(this.moid, CN_NPU_CLASSES, 3);
      return !cns.isEmpty() && (cns.size() >= 2 || !isSlave)?(isSlave?(Util.getIndex((MOID)cns.get(0)) < Util.getIndex((MOID)cns.get(1))?(MOID)cns.get(0):(MOID)cns.get(1)):(cns.size() != 1 && Util.getIndex((MOID)cns.get(0)) <= Util.getIndex((MOID)cns.get(1))?(MOID)cns.get(1):(MOID)cns.get(0))):null;
   }

   private String fetchHWRev(MOID moid) {
      if(moid == null) {
         return "N/A";
      } else {
         String hr = (String)this.resource.getAttribute(moid, "HardwareRevision");
         if(hr == null || hr.trim().equals("")) {
            hr = "N/A";
         }

         return hr;
      }
   }

   private String fetchSWRev(MOID moduleMOID) {
      String softwareRev = null;
      if(moduleMOID != null) {
         softwareRev = (String)this.resource.getAttribute(moduleMOID, "SoftwareRevision");
         return softwareRev != null?softwareRev:"N/A";
      } else {
         return "";
      }
   }

   public AxxEnum getPDHAlarmSummary() {
      String[] toClasses = new String[]{"com.ericsson.mlne.im.common.interfaces.X1"};
      int highSev = 6;
      MOIDList mmuMOID = null;
      MOIDList cnMOID = null;
      MOIDList ltuMOID = null;
      if(this.getIsCN710().booleanValue() || this.getIsCN810().booleanValue() || this.getIsCN810_R2().booleanValue() || this.getIsCN510_R2().booleanValue()) {
         String ltuHighestSev = this.getIsCN710().booleanValue()?"com.ericsson.mlne.im.barebone.npu.CN_710":(this.getIsCN810().booleanValue()?"com.ericsson.mlne.im.barebone.npu.CN_810":(this.getIsCN810_R2().booleanValue()?"com.ericsson.mlne.im.barebone.npu.CN_810_R2":(this.getIsCN510_R2().booleanValue()?"com.ericsson.mlne.im.barebone.npu.CN_510_R2":"")));
         MOIDList cnMoidList = RelationSearchUtil.getChildren(this.moid, ltuHighestSev, 4);
         if(cnMoidList.size() > 0) {
            cnMOID = RelationSearchUtil.getChildren((MOID)cnMoidList.get(0), toClasses, 4);
         }

         MOIDList ltuMoidList = RelationSearchUtil.getChildren(this.moid, "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810", 4);
         if(ltuMoidList.size() > 0) {
            ltuMOID = RelationSearchUtil.getChildren((MOID)ltuMoidList.get(0), toClasses, 4);
         }
      }

      mmuMOID = RelationSearchUtil.getChildren(this.getMMUMaster(), toClasses, 4);
      if(mmuMOID != null) {
         highSev = this.getE1HighestSev(mmuMOID);
      }

      int ltuHighestSev1;
      if(cnMOID != null) {
         ltuHighestSev1 = this.getE1HighestSev(cnMOID);
         highSev = Math.min(ltuHighestSev1, highSev);
      }

      if(ltuMOID != null) {
         ltuHighestSev1 = this.getE1HighestSev(ltuMOID);
         highSev = Math.min(ltuHighestSev1, highSev);
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getPDHAlarmSummaryFromLog(MOID realMoid) {
      return this.getPDHAlarmSummary();
   }

   public AxxEnum getHighTemperatureAlarmSummary() {
      int highSev = 6;
      String[] toClasses = new String[0];
      String[] m;
      if(this.getIsCN710().booleanValue()) {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_710", "com.ericsson.mlne.im.app.etu.physical.LAN_CN_710"};
         toClasses = m;
      } else if(this.getIsCN810_R2().booleanValue()) {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810_R2", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = m;
      } else {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = m;
      }

      MOIDList var10 = RelationSearchUtil.getChildren(this.moid, toClasses, 10);

      for(int i = 0; i < var10.size(); ++i) {
         MOID moid = (MOID)var10.get(i);

         try {
            AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(moid, "HighTemperature");
            highSev = Math.min(sev.value(), highSev);
         } catch (OperationException var8) {
            log.error(var8);
         } catch (MBeanException var9) {
            log.error(var9);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getExcessiveTemperatureAlarmSummary() {
      int highSev = 6;
      String[] toClasses = new String[0];
      String[] m;
      if(this.getIsCN710().booleanValue()) {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_710", "com.ericsson.mlne.im.app.etu.physical.LAN_CN_710"};
         toClasses = m;
      } else if(this.getIsCN810_R2().booleanValue()) {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810_R2", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = m;
      } else {
         m = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = m;
      }

      MOIDList var9 = RelationSearchUtil.getChildren(this.moid, toClasses, 5);

      for(int i = 0; i < var9.size(); ++i) {
         MOID moid = (MOID)var9.get(i);

         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(moid, "ExcessiveTemperature");
            highSev = Math.min(e.value(), highSev);
         } catch (OperationException var7) {
            log.error(var7);
         } catch (MBeanException var8) {
            log.error(var8);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getTrafficFailureAlarmSummary() {
      int highSev = 6;
      String[] toClasses = new String[0];
      String[] trafficFailureAlarmVal;
      if(this.getIsCN710().booleanValue()) {
         trafficFailureAlarmVal = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_710", "com.ericsson.mlne.im.app.etu.physical.LAN_CN_710"};
         toClasses = trafficFailureAlarmVal;
      } else if(this.getIsCN810().booleanValue()) {
         trafficFailureAlarmVal = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = trafficFailureAlarmVal;
      } else {
         trafficFailureAlarmVal = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810_R2", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = trafficFailureAlarmVal;
      }

      int var11 = this.getCN710NEAS("TrafficFailure");
      if(var11 < highSev) {
         highSev = var11;
      }

      MOIDList m = RelationSearchUtil.getChildren(this.moid, toClasses, 5);

      for(int i = 0; i < m.size(); ++i) {
         MOID moid = (MOID)m.get(i);

         try {
            AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(moid, "PowerOrTrafficFailure");
            highSev = Math.min(sev.value(), highSev);
         } catch (OperationException var9) {
            log.error(var9);
         } catch (MBeanException var10) {
            log.error(var10);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getControlFailureAlarmSummary() {
      int highSev = 6;
      String[] toClasses = new String[0];
      String[] e;
      if(this.getIsCN710().booleanValue()) {
         e = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_710", "com.ericsson.mlne.im.app.etu.physical.LAN_CN_710"};
         toClasses = e;
      } else if(this.getIsCN810_R2().booleanValue()) {
         e = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810_R2", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = e;
      } else {
         e = new String[]{"com.ericsson.mlne.im.barebone.npu.CN_810", "com.ericsson.mlne.im.app.ltu.physical.E1_CN_810"};
         toClasses = e;
      }

      try {
         int var10 = this.getCN710NEAS("ControlFailure");
         highSev = Math.min(var10, highSev);
         MOIDList m = RelationSearchUtil.getChildren(this.moid, toClasses, 5);

         for(int i = 0; i < m.size(); ++i) {
            MOID moid = (MOID)m.get(i);
            AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(moid, "ControlSystemFailure");
            highSev = Math.min(sev.value(), highSev);
         }
      } catch (OperationException var8) {
         log.error(var8);
      } catch (MBeanException var9) {
         log.error(var9);
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getRadioLinkAlarmSummary() {
      int highSev = 0;
      MOID tempMMUMasterMoid = this.getMMUMaster();
      if(tempMMUMasterMoid == null) {
         return null;
      } else {
         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(tempMMUMasterMoid, "SummaryAlarm");
            if(e.value() == 1) {
               return new AxxEnum(1, "XF_SEVERITY");
            }

            highSev = Math.max(e.value(), highSev);
            MOID rauIfMoid = RelationSearchUtil.getChild(tempMMUMasterMoid, "com.ericsson.mlne.im.app.rl.common.interfaces.RAU_IF", 3);
            MOID switchMoid = RelationSearchUtil.getChild(tempMMUMasterMoid, "com.ericsson.mlne.im.app.rl.common.interfaces.Switch", 3);
            MOID interfaceMoid = null;

            for(int i = 0; i < 2; ++i) {
               switch(i) {
               case 0:
                  interfaceMoid = rauIfMoid;
                  break;
               case 1:
                  interfaceMoid = switchMoid;
               }

               if(interfaceMoid != null) {
                  AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(interfaceMoid, "SummaryAlarm");
                  if(sev.getConstraintId() == "MLCRAFT_SEVERITY") {
                     sev = this.convertMlcraftToXfSeverity(sev);
                  }

                  highSev = Math.max(sev.value(), highSev);
               }
            }
         } catch (OperationException var9) {
            log.error(var9);
         } catch (MBeanException var10) {
            log.error(var10);
         }

         return new AxxEnum(highSev, "XF_SEVERITY");
      }
   }

   public AxxEnum getRadioLinkAlarmSummaryFromLog(MOID realMoid) {
      return this.getRadioLinkAlarmSummary();
   }

   public AttRef getRMM() {
      return new AttRef(this.getRMMMaster());
   }

   public AxxEnum getCompanionCNRLHighSlotAlarmSummary() {
      int highSev = 0;
      MOID tempMMUSlaveMoid = this.getMMUSlave();
      if(tempMMUSlaveMoid == null) {
         return null;
      } else {
         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(tempMMUSlaveMoid, "SummaryAlarm");
            if(e.value() == 1) {
               return new AxxEnum(1, "XF_SEVERITY");
            }

            highSev = Math.max(e.value(), highSev);
            if(this.getMMUMaster().equals("com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_710") && !tempMMUSlaveMoid.getClassname().equals("com.ericsson.mlne.im.app.rl.pdh.physical.mmu.xf_mlne.MMU_CN_710")) {
               return new AxxEnum(1, "MLCRAFT_SEVERITY");
            }

            MOID rauIfMoid = RelationSearchUtil.getChild(tempMMUSlaveMoid, "com.ericsson.mlne.im.app.rl.common.interfaces.RAU_IF", 3);
            if(rauIfMoid != null) {
               AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(rauIfMoid, "SummaryAlarm");
               highSev = Math.max(sev.value(), highSev);
            }
         } catch (OperationException var6) {
            log.error(var6);
         } catch (MBeanException var7) {
            log.error(var7);
         }

         return new AxxEnum(highSev, "XF_SEVERITY");
      }
   }

   public AxxEnum getRMMAlarmSummaryMaster() {
      int highSev = 6;
      MOID rmmMasterMoid = this.getRMMMaster();
      if(rmmMasterMoid != null) {
         try {
            AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(rmmMasterMoid, "MostSevereAlarm");
            highSev = Math.min(sev.value(), highSev);
         } catch (OperationException var5) {
            log.error(var5);
         } catch (MBeanException var6) {
            log.error(var6);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getRMMAlarmSummaryMasterFromLog(MOID realMoid) {
      return this.getRMMAlarmSummaryMaster();
   }

   public AxxEnum getRMMAlarmSummarySlave() {
      int highSev = 6;
      MOID rmmSlaveMoid = this.getRMMSlave();
      if(rmmSlaveMoid != null) {
         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(rmmSlaveMoid, "MostSevereAlarm");
            highSev = Math.min(e.value(), highSev);
         } catch (OperationException var4) {
            log.error(var4);
         } catch (MBeanException var5) {
            log.error(var5);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getRMMAlarmSummarySlaveFromLog(MOID realMoid) {
      return this.getRMMAlarmSummarySlave();
   }

   public AxxEnum getEthernetAlarmSummary() {
      int highSev = 6;
      boolean isCN710 = this.getIsCN710().booleanValue();
      boolean isCN810 = this.getIsCN810().booleanValue();
      boolean isCN810R2 = this.getIsCN810_R2().booleanValue();
      MOID ethLanWanRlimeEplStatusAndConfigMoid = this.queryEthLanWanRlimeEplStatusAndConfigMoid();
      if(ethLanWanRlimeEplStatusAndConfigMoid == null) {
         return null;
      } else {
         MOID rlimeMoid = this.getInterfaceMoid(ethLanWanRlimeEplStatusAndConfigMoid, "RLIme");
         MOID wanMoid = null;
         if(!isCN710 && !isCN810 && !isCN810R2) {
            wanMoid = this.getInterfaceMoid(ethLanWanRlimeEplStatusAndConfigMoid, "WanInterface");
         }

         MOID lanMoid = this.getInterfaceMoid(ethLanWanRlimeEplStatusAndConfigMoid, "LanInterface");

         try {
            MOID e = null;
            AxxEnum sev;
            int i;
            if(!isCN710 && !isCN810 && !isCN810R2) {
               for(i = 0; i < 3; ++i) {
                  switch(i) {
                  case 0:
                     e = rlimeMoid;
                     break;
                  case 1:
                     e = wanMoid;
                     break;
                  case 2:
                     e = lanMoid;
                  }

                  if(e != null) {
                     sev = (AxxEnum)this.context.getManagementServer().getAttribute(e, "MostSevereAlarm");
                     highSev = Math.min(sev.value(), highSev);
                  }
               }
            } else {
               for(i = 0; i < 2; ++i) {
                  switch(i) {
                  case 0:
                     e = rlimeMoid;
                     break;
                  case 1:
                     e = lanMoid;
                  }

                  if(e != null) {
                     sev = (AxxEnum)this.context.getManagementServer().getAttribute(e, "MostSevereAlarm");
                     highSev = Math.min(sev.value(), highSev);
                  }
               }
            }
         } catch (OperationException var12) {
            log.error(var12);
         } catch (MBeanException var13) {
            log.error(var13);
         }

         return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
      }
   }

   public AxxEnum getEthernetAlarmSummaryFromLog(MOID realMoid) {
      return this.getEthernetAlarmSummary();
   }

   public AxxEnum getUserIOAlarmSummary() {
      AxxEnum userInput = this.getUserInAlarmSummary();
      AxxEnum userOutput = this.getUserOutAlarmSummary();
      return userInput.value() >= userOutput.value()?userOutput:userInput;
   }

   public AxxEnum getUserIOAlarmSummaryFromLog(MOID realMoid) {
      return this.getUserIOAlarmSummary();
   }

   public AxxEnum getUserInAlarmSummary() {
      MOIDList m = RelationSearchUtil.getChildren(this.moid, "com.ericsson.mlne.im.common.physical.User_Input", 4);
      int highSev = 6;

      for(int i = 0; i < m.size(); ++i) {
         MOID userInMoid = (MOID)m.get(i);

         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(userInMoid, "MostSevereAlarm");
            highSev = Math.min(e.value(), highSev);
         } catch (OperationException var6) {
            log.error(var6);
         } catch (MBeanException var7) {
            log.error(var7);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AxxEnum getUserOutAlarmSummary() {
      MOIDList m = RelationSearchUtil.getChildren(this.moid, "com.ericsson.mlne.im.common.physical.User_Output", 4);
      int highSev = 6;

      for(int i = 0; i < m.size(); ++i) {
         MOID userOutMoid = (MOID)m.get(i);

         try {
            AxxEnum e = (AxxEnum)this.context.getManagementServer().getAttribute(userOutMoid, "MostSevereAlarm");
            highSev = Math.min(e.value(), highSev);
         } catch (OperationException var6) {
            log.error(var6);
         } catch (MBeanException var7) {
            log.error(var7);
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public boolean getPowerRedundancyAdminStatus() {
      Object obj = this.resource.getAttribute(this.moid, "PowerRedundancyAdminStatus");
      return obj.equals(new Integer(1));
   }

   public boolean getClockInput() {
      Object obj = this.resource.getAttribute(this.moid, "ClockInput");
      return obj.equals(new Integer(1));
   }

   public void setPowerRedundancyAdminStatus(boolean value) {
      this.resource.setAttribute(this.moid, "PowerRedundancyAdminStatus", new Integer(value?1:2));
   }

   public void setNotificationsCN(boolean value) {
      ArrayList attributes = new ArrayList(3);
      attributes.add("NotificationsCN");
      this.setNotifications(value, attributes);
   }

   public void setNotificationsFAUCN(boolean value) {
      ArrayList attributes = new ArrayList(3);
      attributes.add("FAU4Notifications");
      this.setNotifications(value, attributes);
   }

   public boolean getNotificationsCN() {
      return this.getNotifications("NotificationsCN");
   }

   public boolean getNotificationsFAUCN() {
      return this.getNotifications("FAU4Notifications");
   }

   private void setNotifications(boolean value, List attributes) {
      Integer notifications = Integer.valueOf(2);
      if(value) {
         notifications = Integer.valueOf(1);
      }

      Iterator i$ = attributes.iterator();

      while(i$.hasNext()) {
         String attribute = (String)i$.next();
         this.resource.setAttribute(this.moid, attribute, notifications);
      }

   }

   private boolean getNotifications(String attribute) {
      boolean result = false;
      Integer notifications = (Integer)this.resource.getAttribute(this.moid, attribute);
      if(notifications != null && notifications.equals(Integer.valueOf(1))) {
         result = true;
      }

      return result;
   }

   public void setClockInput(boolean value) {
      this.resource.setAttribute(this.moid, "ClockInput", new Integer(value?1:2));
   }

   public AttRef getSiteLAN() {
      SnmpResource resource = (SnmpResource)this.context.getResource();
      SnmpAdapter snmpAdapter = resource.getAdapter();
      SnmpTable table = snmpAdapter.getTable("xfDcnEthernetConfigEntry", new String[]{"xfDcnEthernetConfigPort"}, 1, (Object)null);
      SnmpTable indexTable = table.getIndexTable();
      if(indexTable.getRowCount() > 0) {
         MOID siteLanMoid = new MOID("com.ericsson.mlcn.im.SiteLAN", "SiteLAN:" + indexTable.getValueAt(0, 0).toString(), this.moid.getResourceId());
         return new AttRef(siteLanMoid);
      } else {
         return null;
      }
   }

   public String getErrorLogViewUserDummy() {
      return "";
   }

   public String getSoftwareUpgradeViewUserDummy() {
      return "";
   }

   private MOID queryEthLanWanRlimeEplStatusAndConfigMoid() {
      MOID queryMoid = new MOID("com.ericsson.mlne.im.fa.ethernet.EthLanWanRlimeEplStatusAndConfig", "dummy", this.moid.getResourceId());
      MOIDList m = this.context.getManagementServer().queryMBeans(queryMoid);
      return m != null && m.size() > 0?(MOID)m.get(0):null;
   }

   private MOID getInterfaceMoid(MOID ethLanWanRlimeEplStatusAndConfigMoid, String interfaceAttribute) {
      if(ethLanWanRlimeEplStatusAndConfigMoid != null) {
         try {
            AttRef e = (AttRef)this.context.getManagementServer().getAttribute(ethLanWanRlimeEplStatusAndConfigMoid, interfaceAttribute);
            if(e != null) {
               return e.getMoid();
            }
         } catch (OperationException var4) {
            log.error(var4);
         } catch (MBeanException var5) {
            log.error(var5);
         }
      }

      return null;
   }

   private String getIndexOfFirstRowFromXFSyncNominatedTable() {
      String idx = null;
      SnmpResource resource = (SnmpResource)this.context.getResource();
      SnmpAdapter snmpAdapter = resource.getAdapter();
      SnmpTable table = snmpAdapter.getTable("xfSyncNominatedEntry", new String[]{"xfSyncNomAssQualityLevel"}, 1, (Object)null);
      if(table.getRowCount() == 1) {
         SnmpTable indexTable = table.getIndexTable();
         idx = (String)indexTable.getValueAt(0, 0);
      }

      return idx;
   }

   public AxxEnum getSyncNomAssQualityLevel() {
      String idx = this.getIndexOfFirstRowFromXFSyncNominatedTable();
      if(idx != null && !"".equals(idx)) {
         SnmpResource resource = (SnmpResource)this.context.getResource();
         SnmpAdapter snmpAdapter = resource.getAdapter();
         String oid = "xfSyncNomAssQualityLevel." + idx;
         Integer val = (Integer)snmpAdapter.getAttribute(oid, (Object)null);
         return new AxxEnum(val.intValue(), "SYNC_NOM_ASS_QUALITY_LEVEL");
      } else {
         return new AxxEnum(5, "SYNC_NOM_ASS_QUALITY_LEVEL");
      }
   }

   public void setSyncNomAssQualityLevel(AxxEnum newValue) {
      String idx = this.getIndexOfFirstRowFromXFSyncNominatedTable();
      if(idx != null && !"".equals(idx)) {
         SnmpResource resource = (SnmpResource)this.context.getResource();
         SnmpAdapter snmpAdapter = resource.getAdapter();
         String oid = "xfSyncNomAssQualityLevel." + idx;
         snmpAdapter.setAttribute(oid, newValue.getValue(), (Object)null);
      } else {
         throw new OperationException("Can not set value of SyncNomAssQualityLevel - no row found.");
      }
   }

   public AxxEnum getCNSyncBoardCapability() {
      int enumVal = 0;
      Object syncBoardCap = this.resourceGetAttribute(this.moid, "CNSyncBoardCapability");
      if(syncBoardCap != null) {
         byte[] bytes = ((String)syncBoardCap).getBytes();
         Hashtable mapper = new Hashtable();
         mapper.put(new BITS_BitSet(new byte[]{(byte)-128}), new Integer(1));
         mapper.put(new BITS_BitSet(new byte[]{(byte)64}), new Integer(2));
         mapper.put(new BITS_BitSet(new byte[]{(byte)-64}), new Integer(3));
         mapper.put(new BITS_BitSet(new byte[]{(byte)0}), new Integer(0));
         Integer v = (Integer)mapper.get(new BITS_BitSet(bytes));
         if(v != null) {
            enumVal = v.intValue();
         }
      }

      return new AxxEnum(enumVal, "CN_SYNC_BOARD_CAPABILITY");
   }

   public AxxEnum getSyncNomQualityLevel() {
      String idx = this.getIndexOfFirstRowFromXFSyncNominatedTable();
      if(idx != null && !"".equals(idx)) {
         SnmpResource resource = (SnmpResource)this.context.getResource();
         SnmpAdapter snmpAdapter = resource.getAdapter();
         String oid = "xfSyncNomQualityLevel." + idx;
         Integer val = (Integer)snmpAdapter.getAttribute(oid, (Object)null);
         return new AxxEnum(val.intValue(), "SYNC_NOM_ASS_QUALITY_LEVEL");
      } else {
         return new AxxEnum("N/A");
      }
   }

   public void setSyncNomQualityLevel(AxxEnum newValue) {
      String idx = this.getIndexOfFirstRowFromXFSyncNominatedTable();
      if(idx != null && !"".equals(idx)) {
         SnmpResource resource = (SnmpResource)this.context.getResource();
         SnmpAdapter snmpAdapter = resource.getAdapter();
         String oid = "xfSyncNomQualityLevel." + idx;
         snmpAdapter.setAttribute(oid, newValue.getValue(), (Object)null);
      } else {
         throw new OperationException("Can not set value of SyncNomAssQualityLevel - no row found.");
      }
   }

   public String getConfigStatusDesc() {
      Integer status = (Integer)this.resource.getAttribute(this.moid, "ConfigStatus");
      String statusInfo = "unknown";
      if(status.intValue() == 1) {
         statusInfo = "Activating";
      }

      if(status.intValue() == 2) {
         statusInfo = "Activation finished";
      }

      if(status.intValue() == 3) {
         statusInfo = "Activation failed";
      }

      if(status.intValue() == 4) {
         statusInfo = "Activating";
      }

      if(status.intValue() == 5) {
         statusInfo = "Activation finished";
      }

      if(status.intValue() == 6) {
         statusInfo = "Activation failed";
      }

      return statusInfo;
   }

   public String getCFHButtonInfoText() {
      Integer status = (Integer)this.resource.getAttribute(this.moid, "ConfigStatus");
      String text = " ";
      if(status.intValue() == 2 || status.intValue() == 3 || status.intValue() == 5 || status.intValue() == 6) {
         text = "Press the OK button to go to the Setup Guide page";
      }

      return text;
   }

   public AxxEnum getRMMBindingStatus() {
      Integer intVal = null;

      try {
         intVal = (Integer)this.resource.getAttribute(this.moid, "NEBIDRStatus");
      } catch (EnvironmentException var3) {
         return null;
      }

      return intVal == null?null:new AxxEnum(intVal.intValue(), "RMM_BINDING_CN_710");
   }

   public Integer getNEBIDRStatus() {
      Integer status = null;

      try {
         status = (Integer)this.resource.getAttribute(this.moid, "NEBIDRStatus");
      } catch (EnvironmentException var3) {
         return Integer.valueOf(0);
      }

      return status == null?Integer.valueOf(0):status;
   }

   public void setSiteLanIPAddress(IPAddress address) {
      MOID siteLanMoid = this.getSiteLAN().getMoid();

      try {
         MgtUtil.getInstance().setAttribute(siteLanMoid, "IPAddress", address);
      } catch (Exception var4) {
         log.error(var4);
      }

   }

   public IPAddress getSiteLanIPAddress() {
      MOID siteLanMoid = this.getSiteLAN().getMoid();

      try {
         IPAddress e = (IPAddress)MgtUtil.getInstance().getAttribute(siteLanMoid, "IPAddress");
         return e;
      } catch (Exception var3) {
         log.error(var3);
         return null;
      }
   }

   public AttributeList setAttributes(AttributeList attributes) throws MBeanException, OperationException {
      boolean conflict = false;
      Attribute attributeDefaultGateWay = null;
      Attribute attributeSiteLan = null;
      if(attributes.getAttribute("IPDefaultGateway") != null && attributes.getAttribute("SiteLanIPAddress") != null) {
         conflict = true;
      }

      if(conflict) {
         LinkedHashMap values = new LinkedHashMap();
         MOID siteLanMoid = this.getSiteLAN().getMoid();
         int index = Util.getIndex(siteLanMoid);
         attributeSiteLan = attributes.removeAttribute("SiteLanIPAddress");
         attributeDefaultGateWay = attributes.removeAttribute("IPDefaultGateway");
         if(attributes.size() > 0) {
            attributes = super.setAttributes(attributes);
         }

         values.put("xfDcnDefaultGateway", attributeDefaultGateWay.getValue());
         values.put("xfDcnEthernetConfigIpAddr." + index, attributeSiteLan.getValue());
         this.getAdapter().setAttributes(values, (Object)null, false);
         attributes.add(attributeDefaultGateWay);
         attributes.add(attributeSiteLan);
         return attributes;
      } else {
         return super.setAttributes(attributes);
      }
   }

   private SnmpAdapter getAdapter() {
      if(this.adapter == null) {
         ServiceLocator locator = ServiceLocator.getInstance();
         MBeanContainer cont = (MBeanContainer)locator.lookup("MBeanContainer", true);

         try {
            ManagedResource e = cont.getMResource(this.resource.getRID());
            if(e instanceof SnmpResource) {
               SnmpResource snmpRes = (SnmpResource)e;
               this.adapter = snmpRes.getAdapter();
            }
         } catch (Exception var5) {
            ;
         }
      }

      return this.adapter;
   }

   public boolean getNetworkSyncSupported() {
      boolean result = false;
      if(this.getIsCNX10().booleanValue()) {
         result = true;
      } else if(this.getCNSyncBoardCapability().value() != 0) {
         result = true;
      }

      return result;
   }

   public Boolean getIsCN210() {
      MOID masterMMU = this.getMMUMaster();
      return MMUUtil.mmuIsCN210(masterMMU);
   }

   public AxxEnum getSfpAlarmSummary() {
      int highSev = 6;
      MOIDList sfpMoids = null;
      if(!this.getIsCN710().booleanValue() && !this.getIsCN810().booleanValue() && !this.getIsCN810_R2().booleanValue()) {
         sfpMoids = RelationSearchUtil.getChildren(this.moid, "com.ericsson.mlne.im.common.physical.SFP", 5);
      } else {
         String[] i = new String[]{"com.ericsson.mlne.im.common.physical.SFPe", "com.ericsson.mlne.im.common.physical.SFPo"};
         sfpMoids = RelationSearchUtil.getChildren(IMUtil.getInstance().getNe(new MOID((String)null, "eqroot", this.moid.getResourceId())), i, 5);
      }

      if(sfpMoids.size() > 0) {
         sfpMoids.removeDuplicates();

         for(int var9 = 0; var9 < sfpMoids.size(); ++var9) {
            MOID sfp = (MOID)sfpMoids.get(var9);

            try {
               Object e = this.context.getManagementServer().getAttribute(sfp, "EquipmentStatusSummary");
               if(e != null && e instanceof AxxEnum) {
                  AxxEnum e1 = (AxxEnum)e;
                  highSev = Math.min(e1.value(), highSev);
               }
            } catch (OperationException var7) {
               log.error(var7);
            } catch (MBeanException var8) {
               log.error(var8);
            }
         }
      }

      return new AxxEnum(highSev, "MLCRAFT_SEVERITY");
   }

   public AttRef getPPP() {
      MOIDList pppMoidList = MgtUtil.getInstance().getMgtService().queryMBeans(new MOID("com.ericsson.mlne.im.common.interfaces.PPP", "PPP:*", this.moid.getResourceId()));
      String SC_1 = "SC1";
      if(pppMoidList != null) {
         Iterator i$ = pppMoidList.iterator();

         while(i$.hasNext()) {
            MOID pppMoid = (MOID)i$.next();
            if(MMUUtil.getAttribute(pppMoid, "Moi").toString().endsWith("SC1")) {
               return new AttRef(pppMoid);
            }
         }
      }

      return null;
   }

   public IPAddress getCN210IPAddress() {
      return (IPAddress)this.getAttribute("IPAddress");
   }

   public void setCN210IPAddress(IPAddress addr) {
      Attribute ipaddress = new Attribute("IPAddress", addr);

      try {
         this.setAttribute(ipaddress);
      } catch (OperationException var4) {
         log.error(var4);
      } catch (MBeanException var5) {
         log.error(var5);
      }

   }

   public Boolean getIsCN510() {
      MOID masterMMU = this.getMMUMaster();
      return MMUUtil.mmuIsCN510(masterMMU);
   }

   public Boolean getIsCN710() {
      return Boolean.valueOf(this.getIsCN710InitialSetupSafe());
   }

   public Boolean getIsCN810() {
      return Boolean.valueOf(MLNEUtil.isType(new NodeType[]{NodeType.CN_810}));
   }

   public Boolean getIsCN810_R2() {
      return Boolean.valueOf(MLNEUtil.isType(new NodeType[]{NodeType.CN_810_R2}));
   }

   public Boolean getIsCN210_1_0() {
      if(this.getIsCN210().booleanValue()) {
         String rev = (String)this.getAttribute("MMUSWRevMaster");
         if(rev != null && "R2".equals(rev.substring(0, 2))) {
            return Boolean.TRUE;
         }
      }

      return Boolean.FALSE;
   }

   public Boolean getIsCNX10() {
      MOID masterMMU = this.getMMUMaster();
      return MMUUtil.mmuIsCNX10(masterMMU);
   }

   public Boolean getIsCNX10Release2() {
      return Boolean.valueOf(this.getIsCNX10().booleanValue() && !this.getIsCN210_1_0().booleanValue());
   }

   public Boolean getIsCN510_R2() {
      MOID masterMMU = this.getMMUMaster();
      return MMUUtil.mmuIsCN510_R2(masterMMU);
   }

   public Boolean getIsCN210_R2() {
      MOID masterMMU = this.getMMUMaster();
      return MMUUtil.mmuIsCN210_R2(masterMMU);
   }

   public Boolean getIsCNX10_R2() {
      return Boolean.valueOf(this.getIsCN210_R2().booleanValue() || this.getIsCN510_R2().booleanValue());
   }

   public String getCNX10ProductNo() {
      String revision = "-";
      String loadModuleTable = "xfSwLoadModuleEntry";
      String productNumberOid = "xfSwLoadModuleProductNumber";
      String descriptionOid = "xfSwLoadModuleDescription";
      String CN_510 = "CN510";
      String CN_210 = "CN210";

      try {
         SnmpResource e = (SnmpResource)this.context.getResource();
         SnmpAdapter snmpAdapter = e.getAdapter();
         SnmpTable table = snmpAdapter.getTable("xfSwLoadModuleEntry", new String[]{"xfSwLoadModuleProductNumber", "xfSwLoadModuleDescription"}, -1, (Object)null);

         for(int i = 0; i < table.getRowCount(); ++i) {
            String descr = table.getValueAt(i, 1).toString();
            if(descr.equals("CN510") || descr.equals("CN210") || descr.equals("CN 710") || descr.equals("CN 810") || descr.equals("CN 810 R2")) {
               return table.getValueAt(i, 0).toString();
            }
         }
      } catch (Exception var12) {
         log.error(var12);
      }

      return revision;
   }

   private boolean getIsCN710InitialSetupSafe() {
      boolean result = false;
      String loadModuleTable = "xfSwBoardTable";
      String productNumberOid = "xfSwBoardProductNumber";

      try {
         SnmpAdapter e = this.getAdapter();
         SnmpTable table = e.getTable("xfSwBoardTable", new String[]{"xfSwBoardProductNumber"}, -1, (Object)null);

         for(int i = 0; i < table.getRowCount(); ++i) {
            String productNumber = table.getValueAt(i, 0).toString();
            if("CXP 901 2516/205".equals(productNumber)) {
               result = true;
               break;
            }
         }
      } catch (Exception var8) {
         log.error(var8);
      }

      return result;
   }

   public boolean getIsNotAssignedTheDefaultValue() {
      ByteArray bits = null;

      try {
         bits = (ByteArray)this.resource.getAttribute(this.moid, "IsNotAssignedTheDefaultValue");
      } catch (Exception var3) {
         ;
      }

      return bits == null?false:BitsWrap.getBitValue(bits.byteValue(), 31);
   }

   public AxxEnum getPowerRedundancyOperStatus() {
      Integer powerRedundancyOperStatus = (Integer)this.resource.getAttribute(this.moid, "PowerRedundancyOperStatus");
      AxxEnum axxitPowerRedundancyOperStatus = new AxxEnum(powerRedundancyOperStatus.intValue());
      return axxitPowerRedundancyOperStatus;
   }

   public Integer getSyncVariant() {
      Integer syncVariant = (Integer)this.resource.getAttribute(this.moid, "SyncVariant", true);
      return syncVariant == null?new Integer(1):syncVariant;
   }

   public Boolean getIsSiteLANNotificationsVisible() {
      Boolean isVisible = new Boolean(Boolean.FALSE.booleanValue());

      try {
         isVisible = (Boolean)MgtUtil.getInstance().getAttribute(this.getSiteLAN().getMoid(), "IsSiteLANNotificationsVisible");
      } catch (Exception var3) {
         log.error(var3);
      }

      return isVisible;
   }

   public Boolean getIsSiteSwitchModeDisplayed() {
      return Boolean.valueOf(MLNEUtil.isType(new NodeType[]{NodeType.CN_R1, NodeType.CN_R2}) && this.isCNX10_1_2OrLater() && !MLNEUtil.isType(new NodeType[]{NodeType.CN_710}) && !MLNEUtil.isType(new NodeType[]{NodeType.CN_810}) && !MLNEUtil.isType(new NodeType[]{NodeType.CN_810_R2}) && !this.isTerminalProtected());
   }

   public Boolean getIsCFHButtonsEnabled() {
      Integer bidrStatus = this.getNEBIDRStatus();
      return this.getIsCNX10().booleanValue() && bidrStatus.intValue() > 0 && bidrStatus.intValue() < 6?Boolean.valueOf(true):(bidrStatus.intValue() != 3 && bidrStatus.intValue() != 4?Boolean.valueOf(false):Boolean.valueOf(true));
   }

   private boolean isCNX10_1_2OrLater() {
      try {
         MOID e = (MOID)this.context.getManagementServer().queryMBeans(new MOID("com.ericsson.mlne.im.fa.ethernet_bridge.BridgeConfigAndStatus", "*", this.moid.getResourceId())).get(0);
         return ((Boolean)MgtUtil.getInstance().getAttribute(e, "ProviderBridgeSupported")).booleanValue();
      } catch (Exception var2) {
         log.error(var2);
         return false;
      }
   }

   private boolean isTerminalProtected() {
      MOID mmu = MMUUtil.getMMUForCN(this.moid, true, true);

      try {
         return ((Boolean)MgtUtil.getInstance().getAttribute(mmu, "IsProtected")).booleanValue();
      } catch (Exception var3) {
         log.error(var3);
         return false;
      }
   }

   public String getSiteSwitchModeReportContent() {
      return this.getIsSiteSwitchModeDisplayed().booleanValue()?((AxxEnum)this.getAttribute("SiteSwitchMode")).getLabel():"";
   }

   public EnumSet getSupportedWizardTypes() {
      return !this.isViewUser() && this.getIsCNX10().booleanValue()?EnumSet.of(MlcWizardTypes.ETH_LAYER1_WIZARD, MlcWizardTypes.ETH_LAYER2_WIZARD):EnumSet.of(MlcWizardTypes.EMPTY);
   }

   public String getSoftwareUpgradeRuleTrigger() {
      return "";
   }

   public String getIsCLISupported() {
      return this.getIsCNX10().booleanValue()?"true":"false";
   }

   public AxxEnum getConfigLoadCommand() {
      return new AxxEnum(0, "BACKUP_RESTORE_OPERATION");
   }

   public Boolean getIsLicenseWarningsSupported() {
      return Boolean.valueOf(false);
   }

   private int getCN710NEAS(String alarmAttriName) {
      MOID NEAlarmStatusMOID = null;
      int neAlarmStatusVal = 0;
      String[] attributes = new String[]{alarmAttriName};

      try {
         NEAlarmStatusMOID = ((AttRef)this.context.getManagementServer().getAttribute(this.moid, "NEAlarmStatus")).getMoid();
         if(NEAlarmStatusMOID != null) {
            ByteArray e = (ByteArray)this.resourceGetAttribute(NEAlarmStatusMOID, "AlarmStatusBits");
            if(e != null) {
               AttributeList result = BitsWrap.getSeveritiesfromBits(attributes, e, attributeCN710ASMapper, "MLCRAFT_SEVERITY");
               neAlarmStatusVal = ((AxxEnum)result.getValue(alarmAttriName)).value();
            }
         }
      } catch (OperationException var7) {
         log.error("OperationException in getCN710NEAlarmStatus" + var7);
      } catch (MBeanException var8) {
         log.error("MBeanException in getCN710NEAlarmStatus" + var8);
      }

      return neAlarmStatusVal;
   }

   private int getE1HighestSev(MOIDList moidList) {
      int highSev = 6;

      for(int i = 0; i < moidList.size(); ++i) {
         MOID e1Moid = (MOID)moidList.get(i);

         try {
            AxxEnum sev = (AxxEnum)this.context.getManagementServer().getAttribute(e1Moid, "MostSevereAlarm");
            highSev = Math.min(sev.value(), highSev);
         } catch (OperationException var7) {
            log.error(var7);
         } catch (MBeanException var8) {
            log.error(var8);
         }
      }

      return highSev;
   }

   public AxxEnum getProtectionRole() {
      Integer roleS = (Integer)this.resourceGetAttribute(this.moid, "ProtectionRole");
      AxxEnum role = new AxxEnum(roleS.intValue(), "CN_ROLE");
      if(role.value() != 3) {
         AxxEnumConstraint constraint = (AxxEnumConstraint)role.getConstraint();
         constraint.removeValues(new int[]{3});
      }

      return role;
   }

   public AxxEnum getProtectionStatusRole() {
      ByteArray status = (ByteArray)this.getAttribute("ProtectionStatus");
      AxxEnum role = BitsWrap.getSeverityFromBits(status, 0);
      return role;
   }

   public AxxEnum getProtectionStatusCable() {
      ByteArray status = (ByteArray)this.getAttribute("ProtectionStatus");
      AxxEnum role = BitsWrap.getSeverityFromBits(status, 1);
      return role;
   }

   public EnumSet getType() {
      EnumSet es = EnumSet.of(NodeType.CN);
      EnumSet id = this.getCnType();
      es.addAll(id);
      return es;
   }

   private EnumSet getCnType() {
      SnmpAdapter snmpAdapter = ((SnmpResource)this.resource).getAdapter();
      String ammMoc = (String)snmpAdapter.getAttribute("entPhysicalDescr.1", true, (Object)null);
      if(ammMoc == null) {
         return EnumSet.of(NodeType.CN_810);
      } else if(ammMoc.endsWith("R2")) {
         return EnumSet.of(NodeType.CN_R2);
      } else if(ammMoc.endsWith("710")) {
         return EnumSet.of(NodeType.CN_R2, NodeType.CN_710);
      } else {
         String mmuMoc;
         if(ammMoc.endsWith("810")) {
            mmuMoc = (String)snmpAdapter.getAttribute("entPhysicalDescr.1954815999", true, (Object)null);
            return mmuMoc.endsWith("810 R2")?EnumSet.of(NodeType.CN_R2, NodeType.CN_810_R2):EnumSet.of(NodeType.CN_R2, NodeType.CN_810);
         } else {
            mmuMoc = (String)snmpAdapter.getAttribute("entPhysicalDescr.1954820095", true, (Object)null);
            return mmuMoc == null?null:(mmuMoc.endsWith("00")?EnumSet.of(NodeType.CN_R0):(mmuMoc.endsWith("10") && !this.isProviderBridgeSupported()?EnumSet.of(NodeType.CN_R1):EnumSet.of(NodeType.CN_R1, NodeType.CN_R12)));
         }
      }
   }

   private boolean isProviderBridgeSupported() {
      SnmpAdapter snmpAdapter = ((SnmpResource)this.resource).getAdapter();
      SnmpTable table = snmpAdapter.getIndexTable("xfEthernetServiceEntry", "xfEthernetServiceVariant", "2", this.context);

      try {
         String e = (String)table.getValueAt(0, 0);
         Integer variant = (Integer)snmpAdapter.getAttribute("xfEthernetServiceVariant." + e, this.context);
         return variant.intValue() >= 6;
      } catch (Exception var5) {
         Logger.getLogger().error("Couldn\'t get Bridge variant!", var5);
         return false;
      }
   }

   public String getBackupConfigFileName() {
      String name = (String)this.resource.getAttribute(this.moid, "Name");
      IPAddress ipAddr = (IPAddress)this.resource.getAttribute(this.moid, "IPAddress");
      String ipAddrStr = ipAddr.toString();
      ipAddrStr = "CN-" + ipAddrStr.replace(".", "-");
      String dtFormat = "_yyyyMMdd_HHmm";
      Date time = (Date)this.resource.getAttribute(this.moid, "SystemClock");
      SimpleDateFormat sdf = new SimpleDateFormat(dtFormat);
      String timeString = sdf.format(time);
      String fileName;
      if(!name.equals("") && !name.equals(ipAddrStr)) {
         fileName = ipAddrStr + "_" + name + timeString + ".cfg";
      } else {
         fileName = ipAddrStr + timeString + ".cfg";
      }

      return fileName;
   }

   public boolean getEthernetSoamSupported() {
      Object value = this.resource.getAttribute(this.moid, "SOAMVariant");
      return value != null && Integer.parseInt(value.toString()) >= 1;
   }

   public String getNodeSwBaseline() {
      Integer activeRelease = (Integer)this.snmpResource.getAdapter().getAttribute("xfSwActiveRelease", (Object)null);
      String release = (String)this.snmpResource.getAdapter().getAttribute("xfSwReleaseRevision." + activeRelease, (Object)null);
      release = release.substring(0, release.lastIndexOf("_"));
      String[] releaseDetail = release.split("_");
      String baseline = StringUtil.join(releaseDetail, " ");
      return baseline;
   }

   private AxxEnum convertMlcraftToXfSeverity(AxxEnum mlcraftSeverity) {
      if(mlcraftSeverity != null && mlcraftSeverity.getConstraintId() == "MLCRAFT_SEVERITY") {
         boolean targetSeverity = false;
         int currentValue = mlcraftSeverity.value();
         byte targetSeverity1;
         switch(currentValue) {
         case 0:
            targetSeverity1 = 1;
            break;
         case 1:
            targetSeverity1 = 5;
            break;
         case 2:
            targetSeverity1 = 4;
            break;
         case 3:
            targetSeverity1 = 3;
            break;
         case 4:
            targetSeverity1 = 2;
            break;
         case 5:
            targetSeverity1 = 0;
            break;
         case 6:
            targetSeverity1 = 6;
            break;
         default:
            targetSeverity1 = -1;
         }

         return new AxxEnum(targetSeverity1, "XF_SEVERITY");
      } else {
         return mlcraftSeverity;
      }
   }

}
