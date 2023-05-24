// CHECKSTYLE:OFF
package test.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.operation.DatabaseOperation;

public class CSVDataSetController {
    private DatabaseConnection dbconn = null;

    public CSVDataSetController() throws Exception {
        dbconn = new DatabaseConnection(DBConnectController.getInstance().getConnect());
        dbconn.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
    }

    public void setUp(final String filePath) throws Exception {

        // CSV用データセット作成
        IDataSet dataset = new CsvDataSet(new File(filePath));
        ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataset);
        replacementDataSet.addReplacementObject("{BLANK}", "");
        replacementDataSet.addReplacementObject("{NULL}", null);

        // データの削除
        DatabaseOperation.DELETE.execute(dbconn, replacementDataSet);
        // データの挿入
        DatabaseOperation.INSERT.execute(dbconn, replacementDataSet);
    }

    /**
     * 予測データ取得
     * @return
     * @throws Exception
     */
    public Map<String, ITable> getExpected(final String file) throws Exception {
        Map<String, ITable> iTableMap = new HashMap<String, ITable>();
        IDataSet expectedDataSet = new XlsDataSet(new File(file));
        for (String tableName : expectedDataSet.getTableNames()) {
            ITable expectedTable = expectedDataSet.getTable(tableName);
            ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(
                    expectedTable, new String[] { "HIREDATE" });
            iTableMap.put(tableName, filteredExpectedTable);
        }

        return iTableMap;
    }

    /**
     * 実績データ取得.
     * DBからtablesで指定したテーブルのデータを取得する。
     * @param tables 取得するテーブル名.
     * @return DBデータ
     * @throws Exception
     */
    public Map<String, ITable> getActualData(final Map<String,ITable> expected) throws Exception {
        Map<String, ITable> iTableMap = new HashMap<String, ITable>();
        IDataSet databaseDataSet = dbconn.createDataSet();
        Iterator<String> it = expected.keySet().iterator();
        while (it.hasNext()) {
            String tableName = it.next();
            ITable actualTable = databaseDataSet.getTable(tableName);
            ITable filteredActualTable = DefaultColumnFilter.includedColumnsTable(
                    actualTable, expected.get(tableName).getTableMetaData().getColumns());
            iTableMap.put(tableName, filteredActualTable);
        }

        return iTableMap;
    }
}
//CHECKSTYLE:ON
