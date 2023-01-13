import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;

import java.io.IOException;

public class SATSolver {
    private DimacsReader dimacsReader;

    public SATSolver(DimacsReader dimacsReader) {
        this.dimacsReader = dimacsReader;
    }

    public IProblem solve(String filename) throws ParseFormatException, IOException, ContradictionException {
        return dimacsReader.parseInstance(filename);
    }

    public String decode(IProblem iProblem) {
        return dimacsReader.decode(iProblem.model());
    }
}
