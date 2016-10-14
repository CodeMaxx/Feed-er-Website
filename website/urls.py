from django.conf.urls import url
from django.contrib import admin

urlpatterns = [
    url(r'^$', views.singnin, name='signin'),
    url(r'^home', views.home, name='home'),
    url(r'^logout', view.logout, name='logout'),
    url(r'^courses$', view.courses, name='view_course'),
    url(r'^courses/add', view.add_courses, name='add_course'),
    url(r'^assignments$', view.assigns, name='view_course'),
    url(r'^assignments/add', view.add_assigns, name='add_course'),
    url(r'^courses/(?P<pk>\S+)/(?P<year>\S+)/(?P<sem>\S+)', view.course_detail, name='c_detail')
]
