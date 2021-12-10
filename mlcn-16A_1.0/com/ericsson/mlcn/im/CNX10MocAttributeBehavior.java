package com.ericsson.mlcn.im;

import com.loox.jloox.LxAbstractText;
import com.loox.jloox.LxAbstractTextArea;
import com.loox.jloox.LxComponent;
import no.axxessit.client.gui.map.Behavior;
import no.axxessit.client.gui.map.DynamicMapEvent;
import no.axxessit.common.val.AxxEnum;

public class CNX10MocAttributeBehavior extends Behavior {

   public CNX10MocAttributeBehavior() {
      super("Show Values", (String[])null);
   }

   public void handleChange(DynamicMapEvent event) {
      LxComponent comp = event.getObject().getComponent();
      Object value = event.getEvent().getValue();
      if(comp instanceof LxAbstractText) {
         ((LxAbstractText)comp).setText(this.getString(value));
      } else if(comp instanceof LxAbstractTextArea) {
         ((LxAbstractTextArea)comp).setText(this.getString(value));
      }

   }

   private String getString(Object value) {
      return value instanceof Exception?"-":(value instanceof AxxEnum?((AxxEnum)value).getLabel():(value == null?"N/A":(value.equals("MMU CN 210")?"CN 210":(value.equals("MMU CN 510")?"CN 510":value.toString()))));
   }
}
