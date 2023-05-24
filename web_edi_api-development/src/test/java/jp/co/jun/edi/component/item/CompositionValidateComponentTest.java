package jp.co.jun.edi.component.item;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.model.MItemModel;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.RegistStatusType;
import lombok.Data;

@RunWith(SpringRunner.class)
public class CompositionValidateComponentTest {
    private static final String TEST_DATA_PATH = "test/jp/co/jun/edi/component/item/CompositionValidateComponentTest";

    private static final String IN_FILE_NAME = "in.json";
    private static final String OUT_FILE_NAME = "out.json";
    private static final String EXPECTED_FILE_NAME = "expected.json";
    private static final String ACTUAL_FILE_NAME = "actual.json";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper OBJECT_MAPPER_FORMAT = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** テストID. */
    private String testId;

    @Spy
    private ItemComponent itemComponent;

    /** モックオブジェクトの挿入対象. */
    @InjectMocks
    private CompositionValidateComponent target;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InFile {
        private MItemModel masterData = null;
        private ItemModel item = null;
        private RegistStatusType registStatus = null;
        private boolean bulkRegist = false;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OutFile {
        private List<ResultMessageEx> resultMessages = null;
        private Exception exception = null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResultMessageEx {
        private MessageCodeType code = null;
        private Object[] args = null;
        private String resource = null;
        private String field = null;
        private Object value = null;
    }

    @Data
    public static class SubResult {
        private String id = null;
        private boolean result = false;
    }

    @Test
    public void main() throws Exception {
        try (Stream<Path> paths = Files.walk(Paths.get(TEST_DATA_PATH))) {
            final List<SubResult> subResults = paths
                    .skip(1)
                    .filter(Files::isDirectory)
                    .map(path -> sub(path))
                    .collect(Collectors.toList());

            subResults.forEach(v -> outputLog("result -> " + v.getId() + " : " + v.isResult()));

            assertThat(subResults.stream().anyMatch(v -> !v.isResult()), is(false));
        }
    }

    private SubResult sub(final Path path) {
        final SubResult subResult = new SubResult();
        subResult.setId(path.getFileName().toString());

        testId = subResult.getId();

        final String dir = path.toString();

        try {
            final InFile in = getInFile(dir + "/" + IN_FILE_NAME);
            final OutFile expected = getOutFile(dir + "/" + OUT_FILE_NAME);
            final OutFile actual = call(in);

            subResult.setResult(compare(expected, actual));
            output(dir, expected, actual);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return subResult;
    }

    private OutFile call(final InFile in) throws Exception {
        final OutFile actual = new OutFile();

        try {
            // パーツマップを取得
            in.getMasterData().setItemPartMap(in.getMasterData().getItemParts().stream().collect(Collectors.toMap(v -> v.getId().toString(), v -> v)));
            // 組成マップを取得
            in.getMasterData().setCompositionMap(in.getMasterData().getCompositions().stream().collect(Collectors.toMap(v -> v.getCode1(), v -> v)));

            actual.setResultMessages(
                    toResultMessagesEx(target.validateCompositions(
                            in.getMasterData(),
                            in.getItem(),
                            in.getRegistStatus(),
                            in.isBulkRegist())));
        } catch (Exception e) {
            e.printStackTrace();
            actual.setException(e);
        }

        return actual;
    }

    private void outputLog(final String str) {
        System.out.println(str);
    }

    private String getFile(final String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    private void outFile(final String dir, final String fileName, final String value) throws Exception {
        Files.write(Paths.get(dir, fileName), value.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }

    private InFile getInFile(final String path) throws Exception {
        final String str = getFile(path);

        outputLog("--- [" + testId + " In File] ---");
        outputLog(str);

        return OBJECT_MAPPER.readValue(str, InFile.class);
    }

    private OutFile getOutFile(final String path) throws Exception {
        final String str = getFile(path);

        // outputLog("--- [" + testId + " Out File] ---");
        // outputLog(str);

        return OBJECT_MAPPER.readValue(str, OutFile.class);
    }

    private List<ResultMessageEx> toResultMessagesEx(final List<ResultMessage> resultMessages) {
        return resultMessages.stream().map(v -> {
            final ResultMessageEx ex = new ResultMessageEx();
            ex.setCode(v.getCode());
            ex.setArgs(v.getArgs());
            ex.setResource(v.getResource());
            ex.setField(v.getField());
            ex.setValue(v.getValue());

            return ex;
        }).collect(Collectors.toList());
    }

    private boolean compare(final OutFile expected, final OutFile actual) throws Exception {
        final String expectedStr = OBJECT_MAPPER.writeValueAsString(expected);
        final String actualStr = OBJECT_MAPPER.writeValueAsString(actual);

        return expectedStr.equals(actualStr);
    }

    private void output(final String dir, final OutFile expected, final OutFile actual) throws Exception {
        final String expectedStr = OBJECT_MAPPER_FORMAT.writeValueAsString(expected);
        final String actualStr = OBJECT_MAPPER_FORMAT.writeValueAsString(actual);

        outputLog("--- [" + testId + " expected] ---");
        outputLog(expectedStr);
        outFile(dir, EXPECTED_FILE_NAME, expectedStr);

        outputLog("--- [" + testId + " actual] ---");
        outputLog(actualStr);
        outFile(dir, ACTUAL_FILE_NAME, expectedStr);
    }
}
