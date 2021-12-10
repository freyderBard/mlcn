package com.ericsson.mlcn.im;

import com.ericsson.mlcraft.common.snmp.AbstractFilteredTableRowProvider;
import com.ericsson.mlcraft.common.snmp.AbstractFilteredTableStrategy;
import no.axxessit.common.val.AxxIntConstraint;
import no.axxessit.mgt.AxxMBTable.RowProvider;

public class NetSyncTableFilterStrategy extends AbstractFilteredTableStrategy {

   private static final Integer PDH_TRAFFIC = Integer.valueOf(1);


   protected RowProvider getRowProvider(final String rowClassname, final String oidPrefix, final String snmpOid, final String parentIndex, final String refSnmpOID, final AxxIntConstraint rowsConstraint) {
      return new AbstractFilteredTableRowProvider(rowClassname, oidPrefix, snmpOid, parentIndex, refSnmpOID, rowsConstraint) {
         protected boolean includeRow(Object referenceColumn, String index) {
            Integer refValue = (Integer)referenceColumn;
            return !NetSyncTableFilterStrategy.PDH_TRAFFIC.equals(refValue);
         }
      };
   }

}
