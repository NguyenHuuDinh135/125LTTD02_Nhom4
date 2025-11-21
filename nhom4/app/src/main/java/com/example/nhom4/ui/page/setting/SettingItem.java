// SettingItem.java
package com.example.nhom4.ui.page.setting;

public class SettingItem {
    private String label;   // Ví dụ: "Tên hiển thị"
    private String value;   // Giá trị hiện tại
    private String field;   // Tên trường Firestore: "username", "birthday", "email"

    public SettingItem(String label, String value, String field) {
        this.label = label;
        this.value = value;
        this.field = field;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getField() { return field; }
}
