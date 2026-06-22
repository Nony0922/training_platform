import request from '@/utils/request'
import { getTeacherScopeParams } from '@/composables/useTeacherScope'

export const getCourseListApi = (scopeMode) => request({
  url: '/course/list',
  method: 'get',
  params: getTeacherScopeParams(scopeMode)
})
export const addCourseApi = (data) => request({ url: '/course/add', method: 'post', data })
export const updateCourseApi = (data) => request({ url: '/course/update', method: 'put', data })
export const deleteCourseApi = (id) => request({ url: `/course/${id}`, method: 'delete' })
