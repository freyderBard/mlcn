package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.common.util.Util;
import com.ericsson.mlne.im.common.MrBean;
import com.ericsson.mlne.utils.MMUUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import no.axxessit.common.Logger;
import no.axxessit.common.val.AxxColorEnum;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.common.val.AxxEnumConstraint;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.MBeanInfo;
import no.axxessit.mgt.MOID;

public class UserInputTable extends MrBean {

   private static final long serialVersionUID = 811866065283096977L;
   private static final int ENABLED = 1;
   private static final int DISABLED = 2;
   private static final int ACTIVE = 1;
   private static final int NA = 0;
   private static final int NOALARM = 1;
   private static final String SEVERITY_ATTR = "Severity";
   private static final String PROBABLE_CAUSE_ATTR = "ProbableCause";
   private static final String SPECIFIC_PROBLEM_ATTR = "SpecificProblem";
   private static final String ACTIVE_WHEN_ATTR = "Config";
   private static final String ENABLE_ATTR = "Enable";
   private static final String STATUS_ATTR = "Status";
   private static final String MOC_ATTR = "Moc";
   private static final String MOI_ATTR = "Moi";
   private static final String USERINPUT_ALARM_SEVERITY = "USERINPUT_ALARM_SEVERITY";
   private static final String USERINPUT_SEVERITY = "USERINPUT_SEVERITY";
   private static final String USERINPUT_CONFIG = "USERINPUT_CONFIG";
   private Map licensedAttributes = new HashMap();


   public UserInputTable(MBeanInfo info) {
      super(info);
   }

   public Map retrieveLicenseAttributes() {
      MOID neMoid = IMUtil.getInstance().getNe(this.moid);
      if(MMUUtil.isCN510_R2(neMoid).booleanValue()) {
         ArrayList licenseInput = new ArrayList();
         licenseInput.add("xfAuxInEnable." + Util.getIndexString(this.moid));
         this.licensedAttributes.put("UserInputTable", licenseInput);
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

   public AxxColorEnum getAlarm() {
      AxxEnumConstraint constraint = (AxxEnumConstraint)this.resource.getConstraintRepository().getConstraint("USERINPUT_ALARM_SEVERITY");
      AxxColorEnum alarm = new AxxColorEnum(0, constraint);
      if(this.getEnable()) {
         if(this.getStatus()) {
            int value = constraint.getValue(this.getSeverity().getLabel());
            if(value == Integer.MIN_VALUE) {
               Logger.getLogger().error("Unable to find same constraint");
               return null;
            }

            alarm.setValue(value);
         } else {
            alarm.setValue(1);
         }
      }

      return alarm;
   }

   public boolean getStatus() {
      return ((Integer)this.resource.getAttribute(this.moid, "Status")).intValue() == 1;
   }

   public AxxEnum getSeverity() {
      AxxEnumConstraint constraint = (AxxEnumConstraint)this.resource.getConstraintRepository().getConstraint("USERINPUT_SEVERITY");
      return new AxxEnum(((Integer)this.resource.getAttribute(this.moid, "Severity")).intValue(), constraint);
   }

   public String getSpecificProblem() {
      return (String)this.resource.getAttribute(this.moid, "SpecificProblem");
   }

   public AxxEnum getConfig() {
      return new AxxEnum(((Integer)this.resource.getAttribute(this.moid, "Config")).intValue(), "USERINPUT_CONFIG");
   }

   public AxxEnum getProbableCause() {
      return new AxxEnum(((Integer)this.resource.getAttribute(this.moid, "ProbableCause")).intValue(), "USER_IN_PROBABLE_CAUSE");
   }

   public void setProbableCause(AxxEnum probableCause) {
      this.resource.setAttribute(this.moid, "ProbableCause", probableCause.getValue());
   }
}
