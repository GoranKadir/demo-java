package com.example.demo.dto;

public class QuotePdfRequest {
  private String html;
  private String url;

  // Getters & setters
  public String getHtml() { return html; }
  public void setHtml(String html) { this.html = html; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }
}