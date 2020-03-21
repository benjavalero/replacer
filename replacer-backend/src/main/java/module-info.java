open module bvalero.replacer {
    requires java.annotation;
    requires java.desktop;
    requires java.persistence;

    requires spring.batch.core;
    requires spring.batch.infrastructure;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires org.hibernate.orm.core;
    requires org.slf4j;

    requires automaton;
    requires lombok;
    requires modelmapper;
    requires org.apache.commons.collections4;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.jetbrains.annotations;
    requires scribejava.apis;
    requires scribejava.core;
}
