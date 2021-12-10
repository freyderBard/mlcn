package com.ericsson.mlcn.im.model;

import com.ericsson.mlcraft.IMC;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.SwingUtilities;
import no.axxessit.client.gui.ApplicationContext;
import no.axxessit.client.gui.DesktopWindow;
import no.axxessit.client.gui.table.AxxMBTableModel;
import no.axxessit.client.gui.table.AxxMBTableModel.StatusRunnable;
import no.axxessit.client.util.Filter;
import no.axxessit.client.util.GUIMOID;
import no.axxessit.common.AxxIterator;
import no.axxessit.common.val.AxxEnum;
import no.axxessit.mgt.Attribute;
import no.axxessit.mgt.AttributeList;
import no.axxessit.mgt.MOID;
import no.axxessit.mgt.MoAttributeList;

public class IproutingTableModel extends AxxMBTableModel implements IMC {

   private static final long serialVersionUID = 1L;
   private ApplicationContext appContext;
   private final int NET_MGMT_VALUE = 3;
   private ArrayList filteredList = new ArrayList();


   public ArrayList getFilteredList() {
      return this.filteredList;
   }

   public IproutingTableModel() {
      super((MOID)null, (Filter)null);
   }

   public IproutingTableModel(Filter filter) {
      super((MOID)null, filter);
   }

   public IproutingTableModel(MOID moid, Filter filter) {
      super(moid, filter);
   }

   public void load(final DesktopWindow app) throws Exception {
      if(this.mbtable != null) {
         AxxIterator iter = this.mbtable.getRowIterator((String[])null);
         int num = iter.size();
         int soFar = 0;
         int batch = iter.getPreferredBatchSize();
         boolean orgMonitorAdd = this.isMonitoringAdd();
         this.appContext = null;
         if(app != null) {
            this.appContext = app.getApplicationContext();
            SwingUtilities.invokeLater(new StatusRunnable(this, app, 5));
         }

         this.setMonitorAdd(false);
         this.inProgress = true;
         this.filteredList.clear();

         while(iter.hasNext() && this.inProgress) {
            Object[] rows = iter.next_n(batch);
            if(num == -2) {
               num = iter.size();
               if(app != null && num == -1) {
                  SwingUtilities.invokeLater(new StatusRunnable(this, app, -2));
               }
            }

            for(int i = 0; i < rows.length; ++i) {
               MoAttributeList row = (MoAttributeList)rows[i];
               AttributeList list = row.getAttributeList();
               int size = list.size();
               LinkedHashMap line = new LinkedHashMap();
               MOID moid = null;

               for(int j = 0; j < size; ++j) {
                  Attribute att = list.getAttribute(j);
                  String name = att.getName();
                  Object value = att.getValue();
                  if(value instanceof MOID) {
                     value = GUIMOID.getInstance((MOID)value);
                  }

                  if(name.equals("Id")) {
                     moid = (MOID)value;
                  }

                  line.put(name, value);
               }

               if(list != null && ((AxxEnum)list.getAttribute("RouteProto").getValue()).value() == 3) {
                  this.add(line, moid);
               } else {
                  this.filteredList.add(line);
               }
            }

            if(app != null && num != -1) {
               SwingUtilities.invokeLater(new StatusRunnable(this, app, (int)((double)(soFar += rows.length) / (double)num * 100.0D)));
            }
         }

         iter.close();
         this.inProgress = false;
         this.setMonitorAdd(orgMonitorAdd);
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               IproutingTableModel.this.evaluateBusinessRules();
               if(app != null) {
                  app.setStatusProgress(100);
                  app.setStatusText("$READY");
               }

            }
         });
      }

   }
}
