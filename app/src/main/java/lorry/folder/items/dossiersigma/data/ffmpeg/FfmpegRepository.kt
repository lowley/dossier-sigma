package lorry.folder.items.dossiersigma.data.ffmpeg

import lorry.folder.items.dossiersigma.data.interfaces.IFfmpegDataSource
import lorry.folder.items.dossiersigma.domain.interfaces.IFfmpegRepository
import javax.inject.Inject

class FfmpegRepository @Inject constructor(val datasource: IFfmpegDataSource) : IFfmpegRepository {


}