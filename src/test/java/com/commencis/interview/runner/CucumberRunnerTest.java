package com.commencis.interview.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Feature dosyalarini calistiran tek giris noktasi. Icinde {@code @Test} yoktur; senaryolari
 * Cucumber engine bulur.
 *
 * <p>Tag burada sabitlenmez: senaryo secimi {@code junit-platform.properties} icindeki
 * {@code cucumber.filter.tags} ile yapilir ve komut satirindan ezilebilir.
 *
 * <pre>
 * .\mvnw.cmd clean verify                                        varsayilan (@api)
 * .\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile"       mobil senaryolar
 * </pre>
 *
 * <p>{@code @SelectPackages("features")} alt klasorleri de kapsar; features/api ve
 * features/mobile icin ayri secici gerekmez.
 */

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
public class CucumberRunnerTest {
}
