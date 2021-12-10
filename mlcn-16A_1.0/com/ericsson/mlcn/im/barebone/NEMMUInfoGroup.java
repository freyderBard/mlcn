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

public class NEMMUInfoGroup extends AttributeGroupHelper implements ICommonStatusLang {

   public boolean isMaster = false;


   public NEMMUInfoGroup(String id, MOIDList moids, MBeanContext context, Boolean master) {
      super(id, (String)null, moids, context);
      this.isMaster = master.booleanValue();
   }

   public String getTitle() {
      if(this.moids != null && this.moids.get(0) != null) {
         this.title = (String)this.context.getResource().getAttribute((MOID)this.moids.get(0), "Moc");
      }

      return this.title;
   }

   protected void addFieldInfo(List fieldInfo) {
      if(!this.moids.isEmpty()) {
         LabelOptions labelOptions = new LabelOptions(true, 4, 1.0D, (String)null);
         ValueOptions valueOptions = new ValueOptions(false, 0, 1.0D, (String)null);
         MOID moid = (MOID)this.moids.get(1);
         if(this.isMaster) {
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "HW Release", moid, "CNHWRevMaster", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "SW Release", moid, "CNSWRevMaster", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "RMM HW Release", moid, "RMMHWRevMaster", (String)null, labelOptions, valueOptions));
         } else {
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "HW Release", moid, "CNHWRevSlave", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "SW Release", moid, "CNSWRevSlave", (String)null, labelOptions, valueOptions));
            fieldInfo.add(new FieldInfo(FieldInfo.TEXT, "RMM HW Release", moid, "RMMHWRevSlave", (String)null, labelOptions, valueOptions));
         }

         fieldInfo.add(new FieldInfo(FieldInfo.SPACE));
      }

   }
}
