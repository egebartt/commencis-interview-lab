package com.commencis.interview.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Feature dosyalarini calistiran tek giris noktasi.
 *
 * <p>Tag burada sabitlenmez; senaryo secimi junit-platform.properties icindeki
 * {@code cucumber.filter.tags} ile yapilir ve komut satirindan ezilebilir:
 *
 * <pre>
 * .\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke"
 * </pre>
 *
 * <p>{@code @SelectClasspathResource} alt klasorleri de kapsar; features/api ve features/mobile
 * icin ayri secici gerekmez.
 *
 * <p>Bu class {@code *Test.java} desenine uydugu icin varsayilan Failsafe kosumlarindan
 * pom.xml'de acikca haric tutulur; yalnizca -Pcucumber profili onu calistirir.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class CucumberRunnerTest {
}
