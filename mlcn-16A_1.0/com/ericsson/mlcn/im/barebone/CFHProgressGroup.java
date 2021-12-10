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

public class CFHProgressGroup extends AttributeGroupHelper implements ICommonStatusLang {

   public CFHProgressGroup(String id, MOIDList moids, MBeanContext context) {
      super(id, (String)null, moids, context);
   }

   protected void addFieldInfo(List fieldInfo) {
      if(!this.moids.isEmpty()) {
         MOID moid = (MOID)this.moids.get(0);
         LabelOptions headerOptions = new LabelOptions(false, 4, 1.0D, (String)null);
         LabelOptions labelOptions = new LabelOptions(false);
         ValueOptions valueOptions = new ValueOptions(false, 0, 1.0D, (String)null);
         fieldInfo.add(new FieldInfo(FieldInfo.LABEL, " Progress:", headerOptions));
         fieldInfo.add(new FieldInfo(FieldInfo.PROGRESS, (String)null, moid, "ConfigProgress", (String)null, labelOptions, valueOptions));
      }
   }
}
