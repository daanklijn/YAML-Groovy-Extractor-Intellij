/*
 * Available context bindings:
 *   COLUMNS     List<DataColumn>
 *   ROWS        Iterable<DataRow>
 *   OUT         { append() }
 *   FORMATTER   { format(row, col); formatValue(Object, col) }
 *   TRANSPOSED  Boolean
 * plus ALL_COLUMNS, TABLE, DIALECT
 *
 * where:
 *   DataRow     { rowNumber(); first(); last(); data(): List<Object>; value(column): Object }
 *   DataColumn  { columnNumber(), name() }
 */

import static com.intellij.openapi.util.text.StringUtil.escapeStringCharacters as escapeStr

NEWLINE = System.getProperty("line.separator")
INDENT = "  "

def printYAML(level, col, o) {
  switch (o) {
    case null: OUT.append(""); break
    case Number: OUT.append(FORMATTER.formatValue(o, col)); break
    case Boolean: OUT.append("$o"); break
    case String: OUT.append("'${escapeStr(o)}'"); break
    case Tuple: printYAML(level, o[0], o[1]); break
    case Map:
      OUT.append("-")
      o.entrySet().eachWithIndex { entry, i ->
        OUT.append("${i != 0 ? "$NEWLINE${INDENT * (level + 1)}" : " "}")
        OUT.append("${escapeStr(entry.getKey().toString())}")
        OUT.append(": ")
        printYAML(level + 1, null, entry.getValue())
      }
      OUT.append("${INDENT * level}")
      break
    case Object[]:
    case Iterable:
      def plain = true
      o.eachWithIndex { item, i ->
        plain = item == null || item instanceof Number || item instanceof Boolean || item instanceof String
        if (plain) OUT.append(i > 0 ? "" : "")
        else OUT.append("${i > 0 ? "" : ""}$NEWLINE${INDENT * (level + 1)}")
        printYAML(level + 1, null, item)
      }
      break
    default:
      if (col != null) printYAML(level, null, FORMATTER.formatValue(o, col))
      else OUT.append("$o")
      break
  }
}

printYAML(-1, null, ROWS.transform { row ->
  def map = new LinkedHashMap<String, String>()
  COLUMNS.each { col ->
    def val = row.value(col)
    map.put(col.name(), new Tuple(col, val))
  }
  map
})
