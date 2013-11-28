package com.emet.customassertion.serialize;

import com.l7tech.policy.assertion.SetsVariables;
import com.l7tech.policy.assertion.UsesVariables;
import com.l7tech.policy.assertion.ext.CustomAssertion;
import com.l7tech.policy.variable.VariableMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An assertion which serializes an object to a BASE64 string.
 */
public class SerializeCustomAssertion implements CustomAssertion, UsesVariables, SetsVariables, Serializable {
    private static final long serialVersionUID = 8570850463501263974L;

    private String inputVarName;
    private String outputVarName;

    public String getName() {
        return "Serialize a variable to BASE64";
    }

    public VariableMetadata[] getVariablesSet() {
        List<VariableMetadata> ret = new ArrayList<VariableMetadata>();
        if (outputVarName != null && outputVarName.length() > 0)
            ret.add(new VariableMetadata(outputVarName));
        return ret.toArray(new VariableMetadata[ret.size()]);
    }

    public String[] getVariablesUsed() {
        List<String> ret = new ArrayList<String>();
        if (inputVarName != null && inputVarName.length() > 0)
            ret.add(inputVarName);
        return ret.toArray(new String[ret.size()]);
    }

    public String getInputVarName() {
        return inputVarName;
    }

    public void setInputVarName(String inputVarName) {
        this.inputVarName = inputVarName;
    }

    public String getOutputVarName() {
        return outputVarName;
    }

    public void setOutputVarName(String outputVarName) {
        this.outputVarName = outputVarName;
    }
}
