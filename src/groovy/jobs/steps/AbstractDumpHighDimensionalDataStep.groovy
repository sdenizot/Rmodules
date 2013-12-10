package jobs.steps

import au.com.bytecode.opencsv.CSVWriter
import jobs.AbstractAnalysisJob
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn

abstract class AbstractDumpHighDimensionalDataStep implements Step {

    final String statusName = null

    File temporaryDirectory
    Closure<Map<String, TabularResult>> resultsHolder

    Map<String, TabularResult> getResults() {
        resultsHolder()
    }

    @Override
    void execute() {
        try {
            writeDefaultCsv results, csvHeader
        } finally {
            results.values().each { it?.close() }
        }
    }

    abstract protected computeCsvRow(String subsetName,
                                     DataRow row,
                                     Long rowNumber,
                                     AssayColumn column,
                                     Object cell)

    abstract List<String> getCsvHeader()

    private void withDefaultCsvWriter(Map<String, TabularResult> results, Closure constructFile) {
        File output = new File(temporaryDirectory, 'outputfile')
        output.createNewFile()
        output.withWriter { writer ->
            CSVWriter csvWriter = new CSVWriter(writer, '\t' as char)
            constructFile.call(csvWriter)
        }
    }

    /* nextRow is a closure with this signature:
     * (String subsetName, DataRow row, Long rowNumber, AssayColumn column, Object cell) -> List<Object> csv row
     */
    private void writeDefaultCsv(Map<String, TabularResult<AssayColumn, DataRow<AssayColumn, Object>>> results,
                                 List<String> header) {

        def doSubset = { String subset,
                         CSVWriter csvWriter ->

            def tabularResult = results[subset]
            if (!tabularResult) {
                return
            }

            def assayList = tabularResult.indicesList

            long i = 0
            tabularResult.each { DataRow row ->
                assayList.each { AssayColumn assay ->
                    if (!row[assay]) {
                        return
                    }

                    def csvRow = computeCsvRow(subset,
                            row,
                            i++,
                            assay,
                            row[assay])

                    csvWriter.writeNext csvRow as String[]
                }
            }
        }

        withDefaultCsvWriter(results) { CSVWriter csvWriter ->

            csvWriter.writeNext header as String[]

            [AbstractAnalysisJob.SUBSET1, AbstractAnalysisJob.SUBSET2].each { subset ->
                doSubset subset, csvWriter
            }
        }
    }

}