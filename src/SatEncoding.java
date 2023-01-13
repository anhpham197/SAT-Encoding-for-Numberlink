import java.util.List;

public class SatEncoding {
    private List<String> rules;
    private int clauses;
    private int variables;

    public SatEncoding(List<String> rules, int clauses, int variables) {
        this.rules = rules;
        this.clauses = clauses;
        this.variables = variables;
    }

    public List<String> getRules() {
        return rules;
    }

    public int getClauses() {
        return clauses;
    }

    public int getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String rule : rules) {
            stringBuilder.append(rule).append("\n");
        }
        return stringBuilder.toString();
    }
}
