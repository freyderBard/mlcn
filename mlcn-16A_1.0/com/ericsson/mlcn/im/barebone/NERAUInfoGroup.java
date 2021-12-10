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

public class NERAUInfoGroup extends AttributeGroupHelper implements ICommonStatusLang {

   public Boolean isMaster = Boolean.valueOf(false);


   public NERAUInfoGroup(String id, MOIDList moids, MBeanContext context, Boolean master) {
      super(id, (String)null, moids, context);
      this.isMaster = master;
   }

   public String getTitle() {
      String title = null;
      if(this.moids != null && this.moids.get(0) != null) {
         title = (String)this.context.getResource().getAttribute((MOID)this.moids.get(0), "Moc");
      }

      return title;
   }

   protected void addFieldInfo(List fieldInfo) {
      if(!this.moids.isEmpty()) {
         LabelOptions labelOptions = new LabelOptions(true, 4, 1.0D, (String)null);
         ValueOptions valueOptions = new ValueOptions(false, 0, 1.0D, (String)null);
         MOID moid = (MOID)this.moids.get(1);
         if(this.isMaster.booleanValue()) {
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "HW Release", moid, "RAUHWRevMaster", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "SW Release", moid, "RAUSWRevMaster", (String)null, labelOptions, valueOptions));
         } else {
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "HW Release", moid, "RAUHWRevSlave", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "SW Release", moid, "RAUSWRevSlave", (String)null, labelOptions, valueOptions));
         }

         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
      }

   }
}
