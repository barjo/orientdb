/*
 * Copyright 2013 Orient Technologies.
 * Copyright 2013 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.sql.functions.OSQLFunction;
import com.orientechnologies.orient.core.sql.method.OSQLMethod;
import com.orientechnologies.orient.core.sql.model.OAnd;
import com.orientechnologies.orient.core.sql.model.OCollection;
import com.orientechnologies.orient.core.sql.model.OContextVariable;
import com.orientechnologies.orient.core.sql.model.OEquals;
import com.orientechnologies.orient.core.sql.model.OExpression;
import com.orientechnologies.orient.core.sql.model.OExpressionVisitor;
import com.orientechnologies.orient.core.sql.model.OName;
import com.orientechnologies.orient.core.sql.model.OIsNotNull;
import com.orientechnologies.orient.core.sql.model.OIsNull;
import com.orientechnologies.orient.core.sql.model.OLiteral;
import com.orientechnologies.orient.core.sql.model.OMap;
import com.orientechnologies.orient.core.sql.model.ONot;
import com.orientechnologies.orient.core.sql.model.OOr;
import com.orientechnologies.orient.core.sql.model.OUnset;
import com.orientechnologies.orient.core.sql.operator.OQueryOperator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CopyVisitor implements OExpressionVisitor {

  @Override
  public Object visit(OLiteral candidate, Object data) {
    //literals are immutable
    return candidate;
  }
  
  @Override
  public Object visit(OCollection candidate, Object data) {
    final List<OExpression> args = candidate.getChildren();
    for(int i=0;i<args.size();i++){
      args.set(i, (OExpression)args.get(i).accept(this, data));
    }
    OCollection copy = new OCollection(args);
    return copy;
  }

  @Override
  public Object visit(OMap candidate, Object data) {
    final LinkedHashMap map = new LinkedHashMap();
    for(Map.Entry<OLiteral,OExpression> entry : candidate.getMap().entrySet()){
      map.put(entry.getKey().accept(this, data), entry.getValue().accept(this, data));
    }
    return new OMap(map);
  }

  @Override
  public Object visit(OName candidate, Object data) {
    //fields are immutable
    return candidate;
  }

  @Override
  public Object visit(OContextVariable candidate, Object data) {
    //context variable are immutable
    return candidate;
  }

  @Override
  public Object visit(OUnset candidate, Object data) {
    //unset are immutable
    return candidate;
  }
  
  @Override
  public Object visit(OSQLFunction candidate, Object data) {
    final List<OExpression> args = new ArrayList<OExpression>(candidate.getArguments());
    for(int i=0;i<args.size();i++){
      args.set(i, (OExpression)args.get(i).accept(this, data));
    }
    OSQLFunction copy = candidate.copy();
    copy.getArguments().clear();
    copy.getArguments().addAll(args);
    return copy;
  }
  
  @Override
  public Object visit(OSQLMethod candidate, Object data) {
    final List<OExpression> args = candidate.getMethodArguments();
    final OExpression source = (OExpression)candidate.getSource().accept(this, data);
    args.add(source);
    for(int i=0;i<args.size();i++){
      args.set(i, (OExpression)args.get(i).accept(this, data));
    }
    OSQLMethod copy = candidate.copy();
    copy.getArguments().clear();
    copy.getArguments().addAll(args);
    return copy;
  }
  
  @Override
  public Object visit(OQueryOperator candidate, Object data) {
    throw new UnsupportedOperationException("Unknowned expression :"+candidate.getClass());
//    final List<OExpression> args = candidate.getArguments();
//    for(int i=0;i<args.size();i++){
//      args.set(i, (OExpression)args.get(i).accept(this, data));
//    }
//    OOperator copy = new OOperator(candidate.getName(), candidate.getAlias(), args);
//    return copy;
  }

  @Override
  public Object visit(OExpression candidate, Object data) {
    throw new UnsupportedOperationException("Unknowned expression :"+candidate.getClass());
  }

  @Override
  public Object visit(OAnd candidate, Object data) {
    return new OAnd(candidate.getAlias(), 
            (OExpression)candidate.getLeft().accept(this,data), 
            (OExpression)candidate.getRight().accept(this,data));
  }

  @Override
  public Object visit(OOr candidate, Object data) {
    return new OOr(candidate.getAlias(), 
            (OExpression)candidate.getLeft().accept(this,data), 
            (OExpression)candidate.getRight().accept(this,data));
  }

  @Override
  public Object visit(ONot candidate, Object data) {
    return new ONot(candidate.getAlias(), 
            (OExpression)candidate.getExpression().accept(this,data));
  }

  @Override
  public Object visit(OEquals candidate, Object data) {
    return new OEquals(candidate.getAlias(), 
            (OExpression)candidate.getLeft().accept(this,data), 
            (OExpression)candidate.getRight().accept(this,data));
  }

  @Override
  public Object visit(OIsNull candidate, Object data) {
    return new OIsNull(candidate.getAlias(), 
            (OExpression)candidate.getExpression().accept(this,data));
  }

  @Override
  public Object visit(OIsNotNull candidate, Object data) {
    return new OIsNotNull(candidate.getAlias(), 
            (OExpression)candidate.getExpression().accept(this,data));
  }

  @Override
  public Object visitInclude(OExpression candidate, Object data) {
    //inmutable
    return candidate;
  }

  @Override
  public Object visitExclude(OExpression candidate, Object data) {
    //inmutable
    return candidate;
  }

}