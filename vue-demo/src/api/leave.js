import request from '@/utils/request'
import { getTeacherScopeParams } from '@/composables/useTeacherScope'

export const getLeaveListApi = () => request({ url: '/leave/list', method: 'get', params: getTeacherScopeParams('homeroom') })
export const updateLeaveApi = (data) => request({ url: '/leave/update', method: 'put', data })
