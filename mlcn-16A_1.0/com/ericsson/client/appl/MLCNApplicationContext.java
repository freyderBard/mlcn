package com.ericsson.client.appl;

import com.ericsson.client.appl.OpenConfigNetSync;
import no.axxessit.client.gui.ApplicationContext;

public class MLCNApplicationContext extends ApplicationContext {

   public MLCNApplicationContext() {
      super("MLCNApp", (Class)null);
   }

   public void initialize() {
      this.add(new OpenConfigNetSync());
   }
}
