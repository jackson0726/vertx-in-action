/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.example.rxjava3.eventbus.rpc;

import io.vertx.rxjava3.RxHelper;
import io.vertx.rxjava3.ObservableHelper;
import io.vertx.rxjava3.FlowableHelper;
import io.vertx.rxjava3.impl.AsyncResultMaybe;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import io.vertx.rxjava3.impl.AsyncResultCompletable;
import io.vertx.rxjava3.WriteStreamObserver;
import io.vertx.rxjava3.WriteStreamSubscriber;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.lang.rx.RxGen;
import io.vertx.lang.rx.TypeArg;
import io.vertx.lang.rx.MappingIterator;


@RxGen(org.example.eventbus.rpc.SensorDataService.class)
public class SensorDataService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SensorDataService that = (SensorDataService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final TypeArg<SensorDataService> __TYPE_ARG = new TypeArg<>(    obj -> new SensorDataService((org.example.eventbus.rpc.SensorDataService) obj),
    SensorDataService::getDelegate
  );

  private final org.example.eventbus.rpc.SensorDataService delegate;
  
  public SensorDataService(org.example.eventbus.rpc.SensorDataService delegate) {
    this.delegate = delegate;
  }

  public SensorDataService(Object delegate) {
    this.delegate = (org.example.eventbus.rpc.SensorDataService)delegate;
  }

  public org.example.eventbus.rpc.SensorDataService getDelegate() {
    return delegate;
  }

  public static org.example.rxjava3.eventbus.rpc.SensorDataService create(io.vertx.rxjava3.core.Vertx vertx) { 
    org.example.rxjava3.eventbus.rpc.SensorDataService ret = org.example.rxjava3.eventbus.rpc.SensorDataService.newInstance((org.example.eventbus.rpc.SensorDataService)org.example.eventbus.rpc.SensorDataService.create(vertx.getDelegate()));
    return ret;
  }

  public static org.example.rxjava3.eventbus.rpc.SensorDataService createProxy(io.vertx.rxjava3.core.Vertx vertx, java.lang.String address) { 
    org.example.rxjava3.eventbus.rpc.SensorDataService ret = org.example.rxjava3.eventbus.rpc.SensorDataService.newInstance((org.example.eventbus.rpc.SensorDataService)org.example.eventbus.rpc.SensorDataService.createProxy(vertx.getDelegate(), address));
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> valueFor(java.lang.String sensorId) { 
    io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> ret = rxValueFor(sensorId);
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> rxValueFor(java.lang.String sensorId) { 
    return AsyncResultSingle.toSingle( handler -> {
      delegate.valueFor(sensorId, handler);
    });
  }

  public io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> average() { 
    io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> ret = rxAverage();
    ret = ret.cache();
    ret.subscribe(io.vertx.rxjava3.SingleHelper.nullObserver());
    return ret;
  }

  public io.reactivex.rxjava3.core.Single<io.vertx.core.json.JsonObject> rxAverage() { 
    return AsyncResultSingle.toSingle( handler -> {
      delegate.average(handler);
    });
  }

  public static SensorDataService newInstance(org.example.eventbus.rpc.SensorDataService arg) {
    return arg != null ? new SensorDataService(arg) : null;
  }

}
