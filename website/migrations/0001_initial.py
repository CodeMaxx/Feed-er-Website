# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2016-10-25 12:41
from __future__ import unicode_literals

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Assignments',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.TextField()),
                ('description', models.TextField()),
                ('deadline', models.DateTimeField(auto_now_add=True)),
            ],
        ),
        migrations.CreateModel(
            name='Course',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.TextField()),
                ('semester', models.IntegerField()),
                ('year', models.DateField(auto_now_add=True)),
                ('course_code', models.CharField(max_length=5)),
            ],
        ),
        migrations.CreateModel(
            name='Feedback',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.TextField()),
                ('deadline', models.DateTimeField(blank=True)),
                ('course', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='website.Course')),
            ],
        ),
        migrations.CreateModel(
            name='FeedbackQuestion',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('question', models.TextField()),
                ('feedback', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='website.Feedback')),
            ],
        ),
        migrations.CreateModel(
            name='FeedbackRatingAnswer',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('rating', models.IntegerField(default=0)),
                ('q', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='website.FeedbackQuestion')),
            ],
        ),
        migrations.CreateModel(
            name='Members',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('fullname', models.CharField(max_length=30)),
                ('email', models.EmailField(max_length=254)),
                ('mtype', models.CharField(choices=[('ST', 'Student'), ('PR', 'Professor'), ('TA', 'Teaching Assistant'), ('AD', 'Admin')], default='ST', max_length=25)),
                ('user', models.OneToOneField(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.AddField(
            model_name='course',
            name='memebers',
            field=models.ManyToManyField(to='website.Members'),
        ),
        migrations.AddField(
            model_name='assignments',
            name='course',
            field=models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='website.Course'),
        ),
    ]
