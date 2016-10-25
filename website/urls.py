from django.conf.urls import url, include
from django.contrib import admin
from . import views

urlpatterns = [
    url(r'^$', views.signin, name='signin'),
    url(r'^signup$', views.signup, name = 'signup'),	# for registering new users
    url(r'^signout', views.logout, name='logout'),
    url(r'^home$', views.home, name='home'),
    url(r'^complete-reg/', views.completeReg, name='complete-reg'),
    url(r'^courses$', views.courses, name='view_course'),
    url(r'^courses/add', views.add_courses, name='add_course'),
    url(r'^assignments$', views.assigns, name='view_course'),
    url(r'^assignments/add', views.add_assigns, name='add_course'),
    url(r'^courses/(?P<pk>\S+)/(?P<year>\S+)/(?P<sem>\S+)', views.course_detail, name='c_detail')
]
