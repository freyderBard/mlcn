package com.ericsson.client.appl;

import com.ericsson.mlne.utils.MMUUtil;
import no.axxessit.client.gui.ApplicationManager;
import no.axxessit.client.gui.action.AxxAction;
import no.axxessit.client.gui.action.AxxParam;
import no.axxessit.client.gui.action.AxxResult;
import no.axxessit.client.gui.action.CommandManager;
import no.axxessit.common.Env;
import no.axxessit.common.val.AxxString;
import no.axxessit.im.util.IMUtil;
import no.axxessit.mgt.MOID;

public class OpenConfigNetSync extends AxxAction {

   private static final long serialVersionUID = -582958347389169423L;


   public OpenConfigNetSync() {
      super("OpenConfigNetSync", "", "");
   }

   public AxxResult execute(AxxParam p) {
      OpenConfigNetSync.MLCNParam param = (OpenConfigNetSync.MLCNParam)p;
      String moidStr = param.moid.value();
      MOID moid = MOID.createMOID(moidStr);
      String file = param.file.value();
      String beanClass = param.beanClass.value();
      String helpId = param.helpId.value();
      String cmdBase = "true".equals(param.openNewWindow.value())?"Browser.Run":"Viewer.Open";
      String cmd = cmdBase + "(address:\'" + file + "?\\\'moid=MOID[" + beanClass + ",NetworkSync," + moid.getResourceId().toString() + "]\\\',help_id=" + helpId + "\')";
      MOID neMoid = IMUtil.getInstance().getNe(new MOID((String)null, "eqroot", moid.getResourceId()));
      if(MMUUtil.isCNX10(neMoid)) {
         file = Env.getInstance().getString("mlcn.mlcn_configure_network_sync");
         cmd = cmdBase + "(address:\'" + file + "?\\\'moid=MOID[" + "com.ericsson.mlcn.im.MLCN" + "," + "eqroot" + "," + moid.getResourceId().toString() + "]\\\',help_id=" + helpId + "\')";
      }

      CommandManager.getInstance().execute(cmd, ApplicationManager.getInstance().getContext());
      return OK;
   }

   public static class MLCNParam extends AxxParam {

      public AxxString moid = AxxString.create((String)null, (String)null, "Managed Object Id");
      public AxxString file = AxxString.create((String)null, (String)null, "File");
      public AxxString beanClass = AxxString.create((String)null, (String)null, "Bean class");
      public AxxString helpId = AxxString.create((String)null, (String)null, "Bean class");
      public AxxString openNewWindow = AxxString.create((String)null, (String)null, "Open in new window");


   }
}
