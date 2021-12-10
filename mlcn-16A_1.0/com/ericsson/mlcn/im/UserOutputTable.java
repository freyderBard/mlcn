package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.common.util.BitsWrap;
import com.ericsson.mlcraft.common.util.Util;
import com.ericsson.mlne.im.common.MrBean;
import com.ericsson.mlne.utils.MMUUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.im.common.ByteArray;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MBeanException;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.OperationException;

public class UserOutputTable extends MrBean {

   private static final long serialVersionUID = 811866065283096977L;
   private static final int ENABLED = 1;
   private static final int DISABLED = 2;
   private static final int ACTIVE = 1;
   private static final int INACTIVE = 2;
   private static final int CONTROLLED_BY_ALARM_SEVERITY = 0;
   private static final int CONTROLLED_BY_OPERATOR_ACTIVE = 1;
   private static final int CONTROLLED_BY_OPERATOR_INACTIVE = 2;
   private static final String NAME_ATTR = "Name";
   private static final String SEVERITY_BITS_ATTR = "SeverityBits";
   private static final String STATUS_ATTR = "Status";
   private static final String ENABLE_ATTR = "Enable";
   private static final String MOC_ATTR = "Moc";
   private static final String MOI_ATTR = "Moi";
   private static final String USEROUTPUT_CONTROLLED_BY = "USEROUTPUT_CONTROLLED_BY";
   private static final String USEROUTPUT_ACTIVE = "USEROUTPUT_ACTIVE";
   private Map licensedAttributes = new HashMap();
   private static HashMap severityToBitsPosMapper = new HashMap();


   public UserOutputTable(MBeanInfo info) {
      super(info);
      severityToBitsPosMapper.put("AlarmSeverity_Operator_Controlled", new Integer(0));
      severityToBitsPosMapper.put("ASC_Cleared", new Integer(5));
      severityToBitsPosMapper.put("ASC_Critical", new Integer(4));
      severityToBitsPosMapper.put("ASC_Major", new Integer(3));
      severityToBitsPosMapper.put("ASC_Minor", new Integer(2));
      severityToBitsPosMapper.put("ASC_Warning", new Integer(1));
   }

   public Map retrieveLicenseAttributes() {
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      if(MMUUtil.isCN510_R2(neMoid).booleanValue() || MMUUtil.isCN810(neMoid) || MMUUtil.isCN810_R2(neMoid)) {
         ArrayList licenseInput = new ArrayList();
         licenseInput.add("xfAuxOutEnable." + Util.getIndexString(this.moid));
         this.licensedAttributes.put("UserOutputTable", licenseInput);
      }

      return this.licensedAttributes;
   }

   public String getLabel() {
      return (String)this.resource.getAttribute(this.moid, "Moc") + " " + (String)this.resource.getAttribute(this.moid, "Moi");
   }

   public boolean getEnable() {
      return ((Integer)this.resource.getAttribute(this.moid, "Enable")).intValue() == 1;
   }

   public void setEnable(boolean enabled) {
      this.resource.setAttribute(this.moid, "Enable", new Integer(enabled?1:2));
   }

   public String getName() {
      return (String)this.resource.getAttribute(this.moid, "Name");
   }

   protected Boolean getSeverityBitPosValue(String name) {
      Boolean severityVal = Boolean.valueOf(false);
      if(severityToBitsPosMapper.containsKey(name)) {
         try {
            ByteArray e = (ByteArray)this.context.getManagementServer().getAttribute(this.moid, "SeverityBits");
            if(e != null) {
               severityVal = Boolean.valueOf(BitsWrap.getBitValue(e.byteValue(), ((Integer)severityToBitsPosMapper.get(name)).intValue()));
            } else {
               log.error("Failed to read severity bits.");
            }
         } catch (OperationException var4) {
            log.error(var4);
         } catch (MBeanException var5) {
            log.error(var5);
         }
      }

      return severityVal;
   }

   public boolean getStatus() {
      return ((Integer)this.resource.getAttribute(this.moid, "Status")).intValue() == 1;
   }

   public void setStatus(AxxEnum active) {
      if(this.getEnable()) {
         this.resource.setAttribute(this.moid, "Status", Integer.valueOf(active.value()));
      }
   }

   public AxxEnum getControlledBy() {
      AxxEnum controlledBy = new AxxEnum(0, "USEROUTPUT_CONTROLLED_BY");
      if(this.getSeverityBitPosValue("AlarmSeverity_Operator_Controlled").booleanValue()) {
         if(this.getActive().value() == 1 && this.getEnable()) {
            controlledBy.setValue(1);
         } else {
            controlledBy.setValue(2);
         }
      }

      return controlledBy;
   }

   public AxxEnum getActive() {
      AxxEnum operatorControlledActive = new AxxEnum(2, "USEROUTPUT_ACTIVE");
      if(((Integer)this.resource.getAttribute(this.moid, "Status")).intValue() == 1) {
         operatorControlledActive.setValue(1);
      }

      return operatorControlledActive;
   }

   public boolean getAlarmSeverityCleared() {
      return this.getSeverityBitPosValue("ASC_Cleared").booleanValue();
   }

   public boolean getAlarmSeverityCritical() {
      return this.getSeverityBitPosValue("ASC_Critical").booleanValue();
   }

   public boolean getAlarmSeverityMajor() {
      return this.getSeverityBitPosValue("ASC_Major").booleanValue();
   }

   public boolean getAlarmSeverityMinor() {
      return this.getSeverityBitPosValue("ASC_Minor").booleanValue();
   }

   public boolean getAlarmSeverityWarning() {
      return this.getSeverityBitPosValue("ASC_Warning").booleanValue();
   }

   public void setSeverityBits(ByteArray bytes) {
      this.resource.setAttribute(this.moid, "SeverityBits", bytes);
   }

   public void setControlledBy(AxxEnum controlledBy) {
      AxxEnum isActive = new AxxEnum(2, "USEROUTPUT_ACTIVE");
      if(controlledBy.value() == 1) {
         isActive.setValue(1);
         this.setStatus(isActive);
      } else if(controlledBy.value() == 2) {
         this.setStatus(isActive);
      }

   }

   public AttributeList setAttributes(AttributeList attributes) throws MBeanException, OperationException {
      AttributeList atts = (AttributeList)attributes.clone();
      if(atts.getAttribute("Enable") != null) {
         super.setAttribute(atts.getAttribute("Enable"));
         atts.removeAllAttributes("Enable");
      }

      return super.setAttributes(atts);
   }

   public void setAlarmSeverityCleared(Boolean state) {}

   public void setAlarmSeverityCritical(Boolean state) {}

   public void setAlarmSeverityMajor(Boolean state) {}

   public void setAlarmSeverityMinor(Boolean state) {}

   public void setAlarmSeverityWarning(Boolean state) {}

}
