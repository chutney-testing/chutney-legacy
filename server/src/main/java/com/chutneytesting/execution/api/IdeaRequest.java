package com.chutneytesting.execution.api;

import java.util.Map;

public class IdeaRequest {
   private String content;
   private Map<String, String> params;

   public IdeaRequest() {
   }

   public IdeaRequest(String content, Map<String, String> params) {
       this.content = content;
       this.params = params;
   }

   public String getContent() {
       return content;
   }

   public void setContent(String content) {
       this.content = content;
   }

   public Map<String, String> getParams() {
       return params;
   }

   public void setParams(Map<String, String> params) {
       this.params = params;
   }
}
