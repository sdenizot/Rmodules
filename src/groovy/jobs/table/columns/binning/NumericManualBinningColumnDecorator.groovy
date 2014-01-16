package jobs.table.columns.binning

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import jobs.table.Column
import jobs.table.columns.ColumnDecorator

class NumericManualBinningColumnDecorator implements ColumnDecorator {

    @Delegate
    Column inner

    List<NumericBinRange> binRanges

    private @Lazy List binNames = {
        def ret = []
        for (i in 0..(binRanges.size() - 1)) {
            def op1
            def lowerBound
            if (i == 0) {
                op1 = '≤'
                lowerBound = binRanges[0].from
            } else if (binRanges[i].from == binRanges[i - 1].to) {
                op1 = '<'
                lowerBound = binRanges[i].from
            } else if (binRanges[i].from > binRanges[i - 1].to) {
                op1 = '≤'
                lowerBound = binRanges[i].from
            } else {
                throw new IllegalStateException("binRanges[$i].from < " +
                        "binRanges[${i - 1}].from. Bad column definition")
            }

            ret << "$lowerBound $op1 $header ≤ ${binRanges[i].to}".toString()
        }
        ret
    }()

    @Override
    Map<String, Object> consumeResultingTableRows() {
        /* if the table rows contain maps, transformValue(Map) will
         * end up being called recursively */
        transformValue inner.consumeResultingTableRows()
    }

    private String transformValue(Object originalValue) {
        transformValue(originalValue as BigDecimal)
    }

    private Object transformValue(Map originalValue) {
        Map transformed = Maps.transformValues(
                originalValue,
                this.&transformValue as Function)

        def filtered = Maps.filterValues transformed,
                { it != null } as Predicate

        /* we have to return a serializable Map */
        ImmutableMap.copyOf filtered
    }

    private String transformValue(Number originalValue) {
        /* not the most efficient implementation... */
        for (i in 0..(binRanges.size() - 1)) {
            if (originalValue >= binRanges[i].from && originalValue <= binRanges[i].to) {
                return binNames[i]
            }
        }
    }

}
