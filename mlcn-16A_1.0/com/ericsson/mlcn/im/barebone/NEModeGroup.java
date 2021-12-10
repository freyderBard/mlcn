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

public class NEModeGroup extends AttributeGroupHelper implements ICommonStatusLang {

   private static final String TRANS_MLCRAFT_SEVERITY = "MLCraft Severity";


   public NEModeGroup(String id, MOIDList moids, MBeanContext context) {
      super(id, (String)null, moids, context);
   }

   protected void addFieldInfo(List fieldInfo) {
      if(!this.moids.isEmpty()) {
         LabelOptions headerOptions = new LabelOptions(true, 4, 1.0D, (String)null);
         LabelOptions labelOptions = new LabelOptions(false);
         ValueOptions alarmValueOptions = new ValueOptions(false, 0, 1.0D, (String)null);
         MOID moid = (MOID)this.moids.get(0);
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, "Mode", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.TEXT, (String)null, moid, "NEAlarmStatus.InstallationModeType", "MLCraft Severity", labelOptions, alarmValueOptions));
      }

   }
}
