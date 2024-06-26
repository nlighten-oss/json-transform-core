package co.nlighten.jsontransform.manipulation;

import co.nlighten.jsontransform.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Taken from https://github.com/egerardus/simple-json-patch/blob/main/src/test/resources/testPatches.json
 */
public class JsonPatchTest extends BaseTest {

    JsonPatch jsonPatch = new JsonPatch<>(adapter);

    public static String read(final String filename) throws FileNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(filename)) {
            if (is == null) {
                throw new FileNotFoundException(filename);
            }
            try (var isr = new InputStreamReader(is, StandardCharsets.UTF_8); var reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Stream tests() throws FileNotFoundException {
        String json = read("JsonPatchTests.json");
        return adapter.jArray.stream(adapter.jArray.type.cast(adapter.parse(json)), true);
    }

    @MethodSource("tests")
    @ParameterizedTest
    void test(final Object testEl) {
        var test = adapter.jObject.convert(testEl);
        if (test.has("disabled") && adapter.getBoolean(test.get("disabled"))) {
            // skip
            return;
        }

        var doc = test.get("doc");
        var patch = adapter.jArray.type.cast(test.get("patch"));
        var hasError = test.has("error");
        if (hasError) {
            Assertions.assertThrows(Exception.class, () -> {
                jsonPatch.patch(patch, doc);
            });
        } else {
            var actual = jsonPatch.patch(patch, doc);
            var message = test.get("comment");
            var expected = test.get("expected");
            Assertions.assertEquals(expected, actual, message == null ? "unexpected" : adapter.getAsString(message));
        }
    }
}
