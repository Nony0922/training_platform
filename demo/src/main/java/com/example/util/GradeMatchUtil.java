package com.example.util;

public final class GradeMatchUtil {

    private GradeMatchUtil() {}

    public static boolean isGradeMatch(String targetGrade, String className) {
        if (targetGrade == null || targetGrade.isBlank()) {
            return true;
        }
        String grade = targetGrade.trim();
        if ("全年级".equals(grade) || "不限".equals(grade)) {
            return true;
        }
        return className != null && className.contains(grade);
    }
}
