package co.nlighten.jsontransform.functions;

import co.nlighten.jsontransform.adapters.JsonAdapter;
import co.nlighten.jsontransform.functions.common.ArgType;
import co.nlighten.jsontransform.functions.common.FunctionContext;
import co.nlighten.jsontransform.functions.common.TransformerFunction;
import co.nlighten.jsontransform.functions.annotations.*;

import java.util.Comparator;

@Aliases("max")
@Documentation("Returns the max of all values in the array")
@InputType(ArgType.Array)

@ArgumentType(value = "default", type = ArgType.Object, position = 0,
              description = "The default value to use for empty values")
@ArgumentType(value = "by", type = ArgType.Transformer, position = 2,
              defaultString = "##current",
              description = "A transformer to extract a property to sum by (using ##current to refer to the current item)")
@ArgumentType(value = "type", type = ArgType.Enum, position = 1, defaultEnum = "AUTO",
              enumValues = { "AUTO", "STRING", "NUMBER", "BOOLEAN" },
              description = "Type of values to expect when ordering the input array")
@OutputType(ArgType.Any)
public class TransformerFunctionMax<JE, JA extends Iterable<JE>, JO extends JE> extends TransformerFunction<JE, JA, JO> {

    public TransformerFunctionMax(JsonAdapter<JE, JA, JO> adapter) {
        super(adapter);
    }
    @Override
    public Object apply(FunctionContext<JE, JA, JO> context) {
        var streamer = context.getJsonElementStreamer(null);
        if (streamer == null || streamer.knownAsEmpty())
            return null;
        var by = context.getJsonElement("by", true);

        var type = context.getEnum("type");

        var def = context.getJsonElement("default",true);
        Comparator<JE> comparator = type == null || "AUTO".equals(type)
                         ? adapter.comparator()
                         : switch (type) {
            case "NUMBER" -> Comparator.comparing(adapter::getNumberAsBigDecimal);
            case "BOOLEAN" -> Comparator.comparing(adapter::getBoolean);
            //case "string"
            default -> Comparator.comparing(adapter::getAsString);
        };
        return streamer.stream()
                .map(t -> {
                    var res = !adapter.isNull(by) ? context.transformItem(by, t) : t;
                    return (adapter.isNull(res)) ? def : res;
                })
                .max(comparator)
                .orElse(adapter.jsonNull());
    }
}
