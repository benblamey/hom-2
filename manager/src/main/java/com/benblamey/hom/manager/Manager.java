package com.benblamey.hom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Manager {


    Logger logger = LoggerFactory.getLogger(class);

    public List<Tier> getTiers() {
        return m_tiers;
    }



    private final List<Tier> m_tiers = new ArrayList<Tier>();

    public void cleanup() throws IOException, InterruptedException {
        String s = Util.executeShellLogAndBlock(new String[]{"kubectl", "get", "pods"});
//        NAME                                          READY   STATUS             RESTARTS   AGE
//        demo-data                                     1/1     Running            1          47h
//        engine-1ba4f755-17f6-49d5-a884-403a2e63f66d   1/1     Running            0          4m5s
//        engine-30c37dc8-fc07-4c8a-a9af-729bc2af63bc   0/1     CrashLoopBackOff   6          10m
//        kafka                                         1/1     Running            0          114s
//        manager                                       1/1     Running            0          28m
        for (String line : s.split("\\n")) {
            if (line.startsWith("engine-")) {
                String pod_name = line.split(" +")[0];
                Util.executeShellLogAndBlock(new String[]{"kubectl", "delete", "pod", pod_name});
            }
        }
    }

    public void addDemoTier() throws IOException, InterruptedException {
        String jexlExpression = "data.foo > " + (m_tiers.size() + 1) * 5;
        addDemoTier(jexlExpression);
    }

    public void addDemoTier(String jexlExpression) throws IOException, InterruptedException {
        String inputTopic = m_tiers.isEmpty() ? "haste-input-data" : m_tiers.get(m_tiers.size() - 1).outputTopic;
        int tierIndex = m_tiers.size();
        Tier tier = new Tier(jexlExpression, tierIndex, inputTopic);
        m_tiers.add(tier);
    }

    public void removeTier() throws IOException, InterruptedException {
        if (m_tiers.isEmpty()) {
            throw new RuntimeException("no tiers exist to remove");
        }
        Tier tier = m_tiers.get(m_tiers.size() - 1);

        tier.setScale(0);
        // TODO - remove old kafka data?

        m_tiers.remove(m_tiers.size() - 1);
    }
}
