import request from '@/utils/request'

export const getAbnormalAttendanceListApi = () => request({ url: '/abnormal-attendance/list', method: 'get' })
export const handleAbnormalAttendanceApi = (id, data) => request({ url: `/abnormal-attendance/${id}/handle`, method: 'put', data })
export const deleteAbnormalAttendanceApi = (id) => request({ url: `/abnormal-attendance/${id}`, method: 'delete' })
