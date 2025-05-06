package com.developerchen.core.logging.evaluator;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * PackageWhitelistEvaluator
 *
 * @author syc
 */
public class PackageWhitelistEvaluator extends EventEvaluatorBase<ILoggingEvent> {


    @Getter
    @Setter
    // Logback 会查找名为 'packages' 的属性或系统属性
    private String packages;
    private final List<String> packageWhitelist = new ArrayList<>();

    @Override
    public void start() {
        if (packages != null && !packages.trim().isEmpty()) {
            // 使用 Logback 的变量替换来解析属性
            String substitutedPackages;
            try {
                substitutedPackages = OptionHelper.substVars(packages, context);
            } catch (ScanException e) {
                substitutedPackages = "";
                addError("whitelist packages configured exception for PackageWhitelistEvaluator", e);
            }
            if (substitutedPackages != null && !substitutedPackages.trim().isEmpty()) {
                String[] pkgs = substitutedPackages.split(",");
                for (String pkg : pkgs) {
                    String trimmedPkg = pkg.trim();
                    if (!trimmedPkg.isEmpty()) {
                        packageWhitelist.add(trimmedPkg);
                    }
                }
            }
        }
        if (packageWhitelist.isEmpty()) {
            addWarn("No whitelisted packages configured for PackageWhitelistEvaluator [" + getName() + "]");
        }
        super.start();
    }

    @Override
    public boolean evaluate(ILoggingEvent event) {
        if (!isStarted() || packageWhitelist.isEmpty()) {
            return false; // 如果未启动或列表为空，则默认拒绝
        }
        String loggerName = event.getLoggerName();
        if (loggerName == null) {
            return false;
        }
        for (String prefix : packageWhitelist) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
