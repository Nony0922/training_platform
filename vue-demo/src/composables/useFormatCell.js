export function useFormatCell() {
  const formatCell = (v, type) => {
    const m = {
      gender: val => (val === 1 ? '男' : val === 2 ? '女' : '-'),
      teacherLevel: val => (val === 2 ? '班主任' : val === 1 ? '任课教师' : '-'),
      teacherStatus: val => (val === 1 ? '在职' : '离职'),
      studentStatus: val => (val === 1 ? '在读' : '离校'),
      status: val => (val === 1 ? '正常' : '停用'),
      shelf: val => (val === 1 ? '上架' : '下架'),
      annStatus: val => (val === 1 ? '已发布' : '草稿'),
      role: val => ({ all: '全部', admin: '管理员', teacher: '教师', parent: '家长' }[val] || val),
      weekday: val => ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][val] || '-',
      teachMode: val => ({ 1: '线下', 2: '线上', 3: '混合' }[val] || '-'),
      progressStatus: val => ['未开始', '进行中', '已完成'][val] || '-',
      examStatus: val => ['未开始', '进行中', '已结束'][val] || '-',
      attendanceStatus: val => ['', '正常', '迟到', '早退', '缺勤', '请假'][val] || '-',
      abnormalType: val => ['', '', '迟到', '早退', '缺勤'][val] || '-',
      handleStatus: val => (val === 1 ? '已处理' : '待处理'),
      leaveType: val => ['', '事假', '病假', '其他'][val] || '-',
      leaveStatus: val => ['待审批', '已通过', '已驳回', '已撤回'][val] || '-',
      visitType: val => ['', '上门', '电话', '线上'][val] || '-',
      orderStatus: val => ['待支付', '已支付', '已取消'][val] || '-',
      msgStatus: val => (val === 1 ? '已回复' : '待回复')
    }
    return (m[type] ? m[type](v) : v) ?? '-'
  }

  return { formatCell }
}
