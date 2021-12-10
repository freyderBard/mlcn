package com.ericsson.mlcn.im.barebone;

import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.FieldInfo;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.LabelOptions;
import com.ericsson.mlcraft.client.gui.forms.nodes.attributegroup.ValueOptions;
import com.ericsson.mlcraft.common.status.ICommonStatusLang;
import com.ericsson.mlcraft.im.common.AttributeGroupHelper;
import java.util.List;
import no.axxessit.mgt.MBeanContext;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MOIDList;

public class NEAlarmGroup extends AttributeGroupHelper implements ICommonStatusLang {

   private static final String TRANS_MLCRAFT_SEVERITY = "MLCraft Severity";
   private boolean hasSlave = false;
   private boolean isProtectionEnabled = false;


   public NEAlarmGroup(String id, MOIDList moids, MBeanContext context, boolean slave, boolean isProtectionEnabled) {
      super(id, (String)null, moids, context);
      this.hasSlave = slave;
      this.isProtectionEnabled = isProtectionEnabled;
   }

   protected void addFieldInfo(List fieldInfo) {
      if(!this.moids.isEmpty()) {
         LabelOptions headerOptions = new LabelOptions(true, 4, 1.0D, (String)null);
         LabelOptions labelOptions = new LabelOptions(false);
         ValueOptions alarmValueOptions = new ValueOptions(false, 0, 1.0D, (String)null);
         MOID moid = (MOID)this.moids.get(0);
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Power Failure DC1", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "NEAlarmStatus.PowerFailureMasterDC1", "MLCraft Severity", labelOptions, alarmValueOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Power Failure DC2", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "NEAlarmStatus.PowerFailureMasterDC2", "MLCraft Severity", labelOptions, alarmValueOptions));
         if(this.hasSlave) {
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Power Failure DC1 Slave", headerOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "NEAlarmStatus.PowerFailureSlaveDC1", "MLCraft Severity", labelOptions, alarmValueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Power Failure DC2 Slave", headerOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "NEAlarmStatus.PowerFailureSlaveDC2", "MLCraft Severity", labelOptions, alarmValueOptions));
         }

         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Low Input Voltage", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "NEAlarmStatus.LowInputVoltage", "MLCraft Severity", labelOptions, alarmValueOptions));
         if(this.isProtectionEnabled) {
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Protection Role", headerOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "ProtectionStatusRole", "MLCraft Severity", labelOptions, alarmValueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Protection Cable", headerOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.ALARM, (String)null, moid, "ProtectionStatusCable", "MLCraft Severity", labelOptions, alarmValueOptions));
         }
      }

   }
}
