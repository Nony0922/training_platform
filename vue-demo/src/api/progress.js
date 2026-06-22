import request from '@/utils/request'
import { getTeacherScopeParams } from '@/composables/useTeacherScope'

export const getProgressListApi = (mode) => request({ url: '/progress/list', method: 'get', params: getTeacherScopeParams(mode) })
export const addProgressApi = (data, mode) => request({ url: '/progress/add', method: 'post', data, params: getTeacherScopeParams(mode) })
export const updateProgressApi = (data, mode) => request({ url: '/progress/update', method: 'put', data, params: getTeacherScopeParams(mode) })
export const deleteProgressApi = (id, mode) => request({ url: `/progress/${id}`, method: 'delete', params: getTeacherScopeParams(mode) })
