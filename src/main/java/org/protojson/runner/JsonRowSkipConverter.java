package org.protojson.runner;

import java.io.IOException;

import org.protojson.pojo.JsonNode;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonRowSkipConverter {

    private String json;
    private final JsonNode root = new JsonNode("root");
    private JsonNode ptr = root;
    private final JsonNode[] row;
    private final String[] result;
    private static String[] EMPTY_RESULT;
    private final int num;
    private int processCount = 0;
    private int currentVersion = 0;

    public JsonRowSkipConverter(String... args) {
        num = args.length;
        row = new JsonNode[num];
        result = new String[num];
        EMPTY_RESULT = new String[num];
        for (int i = 0; i < args.length; i++) {
            EMPTY_RESULT[i] = null;
        }
        for (int i = 0; i < num; i++) {
            JsonNode cur = root;
            String[] paths = args[i].split("\\.");
            for (String path : paths) {
                if (cur.getChildren() == null) {
                    cur._init();
                }
                if (!cur.getChildren().containsKey(path)) {
                    JsonNode child = new JsonNode(path);
                    cur.getChildren().put(path, child);
                    child.setParent(cur);
                }
                cur = cur.getChildren().get(path);
            }
            cur.setLeaf(true);
            row[i] = cur;
        }
    }

    public String[] process(String json) throws IOException {
        this.currentVersion++;
        this.ptr = root;
        processCount = 0;
        this.json = json;
        if (json == null || json.length() == 0) {
            return EMPTY_RESULT;
        }
        for (JsonNode node : row) {
            if (!json.contains(node.getName())) {
                processCount += 1;
            }
        }
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(json);
        parser.nextToken();
        recursion(this.ptr, parser, null);
        return getResult(this.json);
    }

    private void recursion(JsonNode ptr, JsonParser parser, JsonToken skipArray) {
        try {

            while (parser.currentToken() != null) {
                if (processCount == row.length) {
                    return;
                }
                switch (parser.currentToken()) {
                    case START_OBJECT:
                        ptr.setVersion(currentVersion);
                        ptr.setStart(parser.getCurrentLocation().getColumnNr() - 2);
                        parser.nextToken();
                        break;
                    case END_OBJECT:
                        ptr.setEnd(parser.getCurrentLocation().getColumnNr() - 1);
                        //json属性赋值完成
                        processCountAdd(ptr);
                        if (checkComplete()) {
                            //所有关注的属性赋值完成，结束运行
                            return;
                        }
                        ptr = ptr.getParent();
                        parser.nextToken();
                        if (skipArray == JsonToken.END_OBJECT) return;
                        break;
                    case START_ARRAY:
                        ptr.setVersion(currentVersion);
                        ptr.setStart(parser.getCurrentLocation().getColumnNr() - 2);
                        int i = 0;
                        while (parser.currentToken() != JsonToken.END_ARRAY) {
                            parser.nextToken();
                            if (ptr.getChildren().containsKey(String.valueOf(i))) {
                                ptr = ptr.getChildren().get(String.valueOf(i));
                                if (parser.currentToken() == JsonToken.START_OBJECT) {
                                    recursion(ptr, parser, JsonToken.END_OBJECT);
                                } else if (parser.currentToken() == JsonToken.START_ARRAY) {
                                    recursion(ptr, parser, JsonToken.END_ARRAY);
                                } else {
                                    recursion(ptr, parser, JsonToken.NOT_AVAILABLE);
                                }
                                ptr = ptr.getParent();
                            } else {
                                skip(parser);
                            }
                            ++i;
                        }
                        break;
                    case END_ARRAY:
                        ptr.setEnd(parser.getCurrentLocation().getColumnNr() - 1);
                        //array属性赋值完成
                        processCountAdd(ptr);
                        if (checkComplete()) {
                            //所有关注的属性赋值完成，结束运行
                            return;
                        }
                        ptr = ptr.getParent();
                        parser.nextToken();
                        if (skipArray == JsonToken.END_ARRAY) return;
                        break;
                    case FIELD_NAME:
                        if (ptr.getChildren() != null && ptr.getChildren().containsKey(parser.getCurrentName())) {
                            ptr = ptr.getChildren().get(parser.getCurrentName());
                            ptr.setVersion(currentVersion);
                            if (ptr.getChildren() == null) {
                                parser.nextToken();
                                ptr.setValue(skip(parser, true));
                                //常规属性赋值完成（非json和array）
                                processCountAdd(ptr);
                                if (checkComplete()) {
                                    //所有关注的属性赋值完成，结束运行
                                    return;
                                }
                                ptr = ptr.getParent();
                            }
                        } else {
                            parser.nextToken();
                            skip(parser);
                        }
                        parser.nextToken();
                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NULL:
                    case VALUE_NUMBER_INT:
                    case VALUE_TRUE:
                    case VALUE_FALSE:
                        ptr.setVersion(currentVersion);
                        ptr.setValue(parser.getValueAsString());
                        ptr = ptr.getParent();
                        if (skipArray == JsonToken.NOT_AVAILABLE) return;
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCountAdd(JsonNode ptr) {
        if (ptr.isLeaf()) {
            processCount++;
        }
    }

    private boolean checkComplete() {
        return processCount == row.length;
    }

    private String[] getResult(String json) {
        for (int i = 0; i < num; i++) {
            if (row[i].getVersion() < currentVersion) {
                result[i] = null;
                continue;
            }
            if (row[i].getChildren() != null) {
                result[i] = json.substring(row[i].getStart(), row[i].getEnd());
            } else {
                result[i] = row[i].getValue();
            }
        }
        return result;
    }

    private String skip(JsonParser parser, boolean flag) throws IOException {
        this.ptr.setVersion(currentVersion);
        int i = 0, start;
        start = flag ? parser.getCurrentLocation().getColumnNr() - 2 : 0;
        switch (parser.currentToken()) {
            case START_OBJECT:
                i++;
                while (i > 0 && parser.nextToken() != null) {
                    if (parser.currentToken() == JsonToken.START_OBJECT) i++;
                    else if (parser.currentToken() == JsonToken.END_OBJECT) i--;
                }
                return flag ? this.json.substring(start, parser.getCurrentLocation().getColumnNr() - 1) : null;
            case START_ARRAY:
                i++;
                while (i > 0 && parser.nextToken() != null) {
                    if (parser.currentToken() == JsonToken.START_ARRAY) i++;
                    else if (parser.currentToken() == JsonToken.END_ARRAY) i--;
                }
                return flag ? this.json.substring(start, parser.getCurrentLocation().getColumnNr() - 1) : null;
            default:
                return flag ? parser.getValueAsString() : null;
        }
    }

    private void skip(JsonParser parser) throws IOException {
        skip(parser, false);
    }

}
